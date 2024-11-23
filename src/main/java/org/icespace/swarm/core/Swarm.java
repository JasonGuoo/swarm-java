package org.icespace.swarm.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.icespace.swarm.function.annotations.FunctionSpec;
import org.icespace.swarm.function.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.icespace.swarm.llm.model.*;
import org.icespace.swarm.llm.LLMClient;
import org.icespace.swarm.llm.model.FunctionSchema;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main orchestrator for the Swarm framework.
 */
public class Swarm {
    private static final Logger log = LoggerFactory.getLogger(Swarm.class);
    private final LLMClient client;
    private final ErrorHandler errorHandler;
    private final ContextManager contextManager;
    private final ObjectMapper objectMapper;
    private static final String CTX_VARS_NAME = "context_variables";
    private static final int DEFAULT_MAX_TURNS = 10;

    public Swarm(LLMClient client) {
        this.client = client;
        this.errorHandler = new ErrorHandler();
        this.contextManager = new ContextManager();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Run agent with messages and context
     */
    public SwarmResponse run(
            Agent agent,
            List<Message> messages,
            Map<String, Object> contextVariables,
            String modelOverride,
            boolean stream,
            boolean debug,
            int maxTurns) {
        log.info("Starting Swarm execution with agent: {}, stream: {}, maxTurns: {}",
                agent.getClass().getSimpleName(), stream, maxTurns);

        try {
            // if (stream) {
            // log.debug("Using streaming mode");
            // return runAndStream(agent, messages, contextVariables, modelOverride, debug,
            // maxTurns);
            // }

            Agent activeAgent = agent;
            Map<String, Object> context = contextManager.initializeContext(contextVariables);
            log.debug("Initialized context with {} variables", context.size());

            List<Message> history = new ArrayList<>();
            history.addAll(messages);

            int initLen = messages.size();
            int turn = 0;

            while (history.size() - initLen < maxTurns && activeAgent != null) {
                turn++;
                log.debug("Starting turn {}/{}", turn, maxTurns);

                try {
                    if (debug) {
                        log.debug("Current history before completion:");
                        printHistory(history);
                    }

                    ChatResponse completion = getChatCompletion(
                            activeAgent, history, context, modelOverride, stream, debug);

                    checkResponseError(completion);

                    // Break if no tool calls - task is complete
                    if (!hasToolCalls(completion)) {
                        log.debug("No tool calls in response, ending turn");
                        // Add the response message to history
                        if (completion.getChoices() != null && !completion.getChoices().isEmpty()) {
                            Message responseMessage = completion.getChoices().get(0).getMessage();
                            history.add(responseMessage);
                        }
                        break;
                    }

                    // Add the response message to history
                    if (completion.getChoices() != null && !completion.getChoices().isEmpty()) {
                        Message responseMessage = completion.getChoices().get(0).getMessage();
                        history.add(responseMessage);
                        // Handle any tool calls
                        if (hasToolCalls(completion)) {
                            Object result = handleToolCalls(completion, activeAgent, history, context);
                            if (result instanceof Agent) {
                                activeAgent = (Agent) result;
                            }
                        }
                    }

                    if (debug) {
                        log.debug("Updated history after processing response:");
                        printHistory(history);
                    }

                } catch (Exception e) {
                    log.error("Error during turn {}: {}", turn, e.getMessage());
                    return SwarmResponse.builder()
                            .history(history)
                            .activeAgent(activeAgent)
                            .context(context)
                            .build();
                }
            }

            log.info("Swarm execution completed successfully after {} turns", turn);
            return SwarmResponse.builder()
                    .history(history)
                    .activeAgent(activeAgent)
                    .context(context)
                    .build();

        } catch (Exception e) {
            log.error("Fatal error in Swarm execution: {}", e.getMessage(), e);
            throw new SwarmException("Fatal error in Swarm execution", e);
        }
    }

    /**
     * Prints the given conversation history in beautified JSON format.
     * This is useful for debugging and understanding the data flow.
     * 
     * @param history The conversation history to print
     */
    private static void printHistory(List<Message> history) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(history);
            log.debug("\nConversation History:\n{}", prettyJson);
        } catch (Exception e) {
            log.error("Error printing history: {}", e.getMessage());
        }
    }

    private ChatResponse handleError(Exception e, Agent agent, Map<String, Object> context) {
        log.error("Handling error: {}", e.getMessage());

        try {
            errorHandler.saveState(context);
            log.debug("Saved context state");

            if (errorHandler.canRetry(e)) {
                log.info("Attempting to retry operation");
                return retryOperation(agent, context);
            }
            // else if (errorHandler.needsFallback(e)) {
            // log.info("Switching to fallback agent");
            // Agent fallbackAgent = errorHandler.getFallbackAgent();
            // return fallbackAgent.execute(buildRequest(fallbackAgent, new ArrayList<>(),
            // context, null), context);
            // }
            else {
                log.error("Fatal error, restoring state");
                errorHandler.restoreState(context);
                throw new SwarmException("Fatal error occurred", e);
            }
        } catch (Exception ex) {
            log.error("Error recovery failed: {}", ex.getMessage());
            throw new SwarmException("Error recovery failed", ex);
        }
    }

    private ChatRequest buildRequest(Agent agent, List<Message> history, Map<String, Object> context,
            String modelOverride) {
        String instructions = agent.getSystemPrompt(context);

        // Build base request
        ChatRequest request = new ChatRequest();
        if (modelOverride != null) {
            request.setModel(modelOverride);
        }

        request.setMessages(buildMessages(instructions, history));

        // Handle tool choice according to the workflow
        if (agent.getToolChoice() != null) {
            switch (agent.getToolChoice()) {
                case AUTO:
                    // Use all available functions
                    List<Map<String, Object>> tools = agent.getTools();
                    List<FunctionSchema> functions = new ArrayList<>();

                    if (tools != null) {
                        for (Map<String, Object> tool : tools) {
                            try {
                                Map<String, Object> function = (Map<String, Object>) tool.get("function");
                                if (function != null) {
                                    FunctionSchema schema = new FunctionSchema();
                                    schema.setName((String) function.get("name"));
                                    schema.setDescription((String) function.get("description"));
                                    schema.setParameters((Map<String, Object>) function.get("parameters"));
                                    functions.add(schema);
                                }
                            } catch (Exception e) {
                                log.warn("Failed to convert tool to function schema: {}", tool, e);
                            }
                        }
                    }

                    if (!functions.isEmpty()) {
                        request.setFunctions(functions);
                        request.setFunctionCall("auto");
                    }
                    break;

                case NONE:
                    // Explicitly disable function calling
                    request.setFunctionCall("none");
                    break;
            }
        }

        return request;
    }

    /**
     * Helper method to validate and prepare functions for tool choice
     */
    private List<FunctionSchema> validateFunctions(List<FunctionSchema> functions) {
        if (functions == null || functions.isEmpty()) {
            return Collections.emptyList();
        }

        // Validate each function schema
        return functions.stream()
                .filter(Objects::nonNull)
                .filter(f -> f.getName() != null && !f.getName().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Run agent with streaming support
     */
    private ChatResponse runAndStream(
            Agent agent,
            List<Message> messages,
            Map<String, Object> contextVariables,
            String modelOverride,
            boolean debug,
            int maxTurns) {
        Map<String, Object> context = contextManager.initializeContext(contextVariables);
        List<Message> history = new ArrayList<>();

        // Add system prompt as first message
        history.add(Message.builder()
                .role("system")
                .content(agent.getSystemPrompt(context))
                .build());
        history.addAll(messages);

        List<Message> responseMessages = new ArrayList<>();

        try (Stream<ChatResponse> stream = client.stream(
                buildRequest(agent, history, context, modelOverride))) {

            stream.forEach(response -> {
                if (debug) {
                    log.debug("Stream chunk: {}", response);
                }

                processStreamChunk(response, responseMessages, context);

                // Check for tool calls in the chunk
                if (hasToolCalls(response)) {
                    Object result = handleToolCalls(response, agent, history, context);
                }
            });
        }

        return buildFinalResponse(history, agent, context);
    }

    private void checkResponseError(Map<String, Object> completion) {
        Map<String, Object> error = (Map<String, Object>) completion.get("error");
        if (error != null) {
            String errorMessage = error.containsKey("message") ? error.get("message").toString() : "Unknown error";
            int errorCode = error.containsKey("code") ? Integer.parseInt(error.get("code").toString()) : 0;

            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = error.containsKey("metadata") ? (Map<String, Object>) error.get("metadata")
                    : Collections.emptyMap();

            String rawError = metadata.containsKey("raw") ? metadata.get("raw").toString() : "";
            String provider = metadata.containsKey("provider_name") ? metadata.get("provider_name").toString()
                    : "Unknown";

            log.error("LLM error from {}: {} (code: {})\nRaw error: {}",
                    provider, errorMessage, errorCode, rawError);

            throw new SwarmException(String.format("LLM error: %s (provider: %s)", errorMessage, provider));
        }
    }

    private void checkResponseError(ChatResponse completion) {
        if (completion == null) {
            throw new SwarmException("Received null ChatResponse");
        }

        // Check for error using dynamic field access
        Map<String, Object> error = completion.getFieldValue("error", Map.class);
        if (error != null && !error.isEmpty()) {
            String errorMessage = error.containsKey("message") ? error.get("message").toString() : "Unknown error";
            int errorCode = error.containsKey("code") ? Integer.parseInt(error.get("code").toString()) : 0;

            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = error.containsKey("metadata") ? (Map<String, Object>) error.get("metadata")
                    : Collections.emptyMap();

            String rawError = metadata.containsKey("raw") ? metadata.get("raw").toString() : "";
            String provider = metadata.containsKey("provider_name") ? metadata.get("provider_name").toString()
                    : "Unknown";

            log.error("LLM error from {}: {} (code: {})\nRaw error: {}",
                    provider, errorMessage, errorCode, rawError);

            throw new SwarmException(String.format("LLM error: %s (provider: %s)", errorMessage, provider));
        }
    }

    /**
     * Build messages including system instructions
     */
    private List<Message> buildMessages(String instructions, List<Message> history) {
        List<Message> messages = new ArrayList<>();

        // Add system message with instructions
        messages.add(Message.builder()
                .role("system")
                .content(instructions)
                .build());

        // Add conversation history
        messages.addAll(history);

        return messages;
    }

    /**
     * Check if response has tool calls
     */
    private boolean hasToolCalls(ChatResponse response) {
        return response.getChoices() != null &&
                !response.getChoices().isEmpty() &&
                response.getChoices().get(0).getMessage() != null &&
                response.getChoices().get(0).getMessage().getToolCalls() != null &&
                response.getChoices().get(0).getMessage().getToolCalls().length > 0;
    }

    /**
     * Handle tool calls in response
     */
    private Object handleToolCalls(
            ChatResponse response,
            Agent agent,
            List<Message> history,
            Map<String, Object> context) throws SwarmException {
        Message message = response.getChoices().get(0).getMessage();
        ToolCall[] toolCalls = message.getToolCalls();

        log.debug("Processing {} tool calls", toolCalls.length);
        Object result = null;

        for (ToolCall toolCall : toolCalls) {
            try {
                String functionName = toolCall.getFunction().getName();
                log.debug("Executing tool call: {}", functionName);

                // Get function spec and validate
                Map<String, Object> functionSpec = agent.getFunctionSpec(functionName);
                if (functionSpec == null) {
                    throw new SwarmException("Function not found: " + functionName);
                }

                // Parse arguments
                Map<String, Object> arguments = objectMapper.readValue(
                        toolCall.getFunction().getArguments(),
                        new TypeReference<Map<String, Object>>() {
                        });

                // Get method and parameters
                Method method = agent.findFunction(functionName);
                java.lang.reflect.Parameter[] methodParams = method.getParameters();
                if (methodParams == null || methodParams.length == 0) {
                    throw new SwarmException("No parameters defined for function: " + functionName);
                }

                // Prepare arguments with default values
                Object[] args = new Object[methodParams.length];
                for (int i = 0; i < methodParams.length; i++) {
                    java.lang.reflect.Parameter methodParam = methodParams[i];
                    Parameter paramAnnotation = methodParam.getAnnotation(Parameter.class);

                    // Check specifically for 'context' parameter
                    if (methodParam.getName().equals("context") &&
                            methodParam.getType().equals(Map.class) &&
                            methodParam.getParameterizedType().getTypeName()
                                    .equals("java.util.Map<java.lang.String, java.lang.Object>")) {
                        args[i] = context;
                        continue;
                    }

                    if (paramAnnotation == null) {
                        // Skip parameters without @Parameter annotation
                        continue;
                    }

                    String paramName = methodParam.getName();
                    Object value = arguments.get(paramName);

                    if (value == null && !paramAnnotation.defaultValue().isEmpty()) {
                        // Use default value if provided
                        value = convertArgument(paramAnnotation.defaultValue(), methodParam.getType());
                    } else if (value == null) {
                        throw new SwarmException("Required parameter missing: " + paramName);
                    }

                    args[i] = convertArgument(value, methodParam.getType());
                }

                // Invoke the function
                result = method.invoke(agent, args);

            } catch (Exception e) {
                log.error("Error executing tool call {}: {}",
                        toolCall.getFunction().getName(), e.getMessage());
                throw new SwarmException("Tool call execution failed", e);
            }
        }
        return result;
    }

    /**
     * Convert argument to the target type
     */
    private Object convertArgument(Object value, Class<?> targetType) {
        if (value == null)
            return null;

        try {
            if (targetType == String.class) {
                return value.toString();
            } else if (targetType == Integer.class || targetType == int.class) {
                return Integer.valueOf(value.toString());
            } else if (targetType == Double.class || targetType == double.class) {
                return Double.valueOf(value.toString());
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.valueOf(value.toString());
            }

            // For complex types, use Jackson's conversion
            return objectMapper.convertValue(value, targetType);
        } catch (Exception e) {
            throw new SwarmException("Failed to convert argument to type " + targetType.getName(), e);
        }
    }

    /**
     * Process a streaming chunk
     */
    private void processStreamChunk(
            ChatResponse chunk,
            List<Message> responseMessages,
            Map<String, Object> context) {
        if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
            Message message = chunk.getChoices().get(0).getMessage();
            if (message != null) {
                responseMessages.add(message);
            }
        }
    }

    /**
     * Build final response
     */
    private ChatResponse buildFinalResponse(
            List<Message> history,
            Agent agent,
            Map<String, Object> context) {
        return ChatResponse.builder()
                .choices(Collections.singletonList(
                        Choice.builder()
                                .message(history.get(history.size() - 1))
                                .build()))
                .rawJson(objectMapper.valueToTree(context))
                .build();
    }

    /**
     * Context management inner class
     */
    private class ContextManager {
        private final ReadWriteLock lock = new ReentrantReadWriteLock();

        public Map<String, Object> initializeContext(Map<String, Object> initial) {
            lock.writeLock().lock();
            try {
                Map<String, Object> context = new HashMap<>(initial);
                validateContextUpdates(context);
                cleanSensitiveData(context);
                return context;
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void updateContext(Map<String, Object> updates, Map<String, Object> context) {
            lock.writeLock().lock();
            try {
                validateContextUpdates(updates);
                context.putAll(updates);
                cleanSensitiveData(context);
                notifyContextUpdated(context);
            } finally {
                lock.writeLock().unlock();
            }
        }

        private void validateContextUpdates(Map<String, Object> updates) {
            if (updates == null)
                return;

            // Validate context size
            if (updates.size() > 100) {
                throw new SwarmException("Context update too large: " + updates.size() + " entries");
            }

            // Validate value types and sizes
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // Check key format
                if (!key.matches("^[a-zA-Z0-9_]+$")) {
                    throw new SwarmException("Invalid context key format: " + key);
                }

                // Check value type
                if (value != null && !(value instanceof String ||
                        value instanceof Number ||
                        value instanceof Boolean ||
                        value instanceof Map ||
                        value instanceof List)) {
                    throw new SwarmException("Invalid context value type for key: " + key);
                }

                // Check string length
                if (value instanceof String && ((String) value).length() > 10000) {
                    throw new SwarmException("Context string value too long for key: " + key);
                }
            }
        }

        private void cleanSensitiveData(Map<String, Object> context) {
            // Remove temporary and sensitive data
        }

        private void notifyContextUpdated(Map<String, Object> context) {
            // Notify any observers of context changes
        }
    }

    /**
     * Error handling inner class
     */
    private class ErrorHandler {
        private Map<String, Object> savedState;

        public void saveState(Map<String, Object> context) {
            this.savedState = new HashMap<>(context);
        }

        public void restoreState(Map<String, Object> context) {
            if (savedState != null) {
                context.clear();
                context.putAll(savedState);
            }
        }

        public boolean canRetry(Exception e) {
            // Implement retry decision logic
            return false;
        }

        public boolean needsFallback(Exception e) {
            // Implement fallback decision logic
            return false;
        }

        public Agent getFallbackAgent() {
            // Implement fallback agent creation
            return null;
        }
    }

    /**
     * Get chat completion from LLM
     */
    private ChatResponse getChatCompletion(
            Agent agent,
            List<Message> history,
            Map<String, Object> context,
            String modelOverride,
            boolean stream,
            boolean debug) {
        log.debug("Getting chat completion from LLM");
        try {
            ChatRequest request = buildRequest(agent, history, context, modelOverride);

            if (debug) {
                log.debug("Request to LLM: {}",
                        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
            }

            ChatResponse response = client.chat(request);

            if (debug) {
                log.debug("Response from LLM: {}",
                        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
            }

            return response;
        } catch (Exception e) {
            log.error("Failed to get chat completion: {}", e.getMessage());
            throw new SwarmException("Failed to get chat completion", e);
        }
    }

    /**
     * Retry an operation with the agent
     */
    private ChatResponse retryOperation(Agent agent, Map<String, Object> context) {
        // Implement retry logic here
        // For example, rebuild the request and try again with backoff
        try {
            Thread.sleep(1000); // Simple backoff
            return getChatCompletion(agent, new ArrayList<>(), context, null, false, false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SwarmException("Retry interrupted", e);
        }
    }

    /**
     * Custom exception for Swarm operations
     */
    public static class SwarmException extends RuntimeException {
        public SwarmException(String message) {
            super(message);
        }

        public SwarmException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private List<Map<String, Object>> getFunctionsForToolChoice(Agent agent) {
        if (agent.getToolChoice() == null)
            return Collections.emptyList();

        switch (agent.getToolChoice()) {
            case AUTO:
                return agent.getTools();
            case NONE:
                return Collections.emptyList();
            default:
                return Collections.emptyList();
        }
    }
}