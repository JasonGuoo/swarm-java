package org.icespace.swarm.core;

import org.icespace.swarm.function.annotations.FunctionSpec;
import org.icespace.swarm.function.annotations.Parameter;
import org.icespace.swarm.llm.model.ChatRequest;
import org.icespace.swarm.llm.model.ChatResponse;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract base class for AI agents in the Swarm framework.
 * 
 * An Agent defines the capabilities and behavior of an AI assistant through:
 * 1. A system prompt that sets the agent's role and guidelines
 * 2. A set of tools (functions) that the agent can use
 * 3. A tool choice mode that determines how tools are selected
 * 
 * Tools are discovered automatically through method annotations:
 * - Use @FunctionSpec to define a tool
 * - Use @Parameter to define tool parameters
 * - A special parameter named 'context' of type Map<String, Object> can be added 
 *   without @Parameter annotation to access the current execution context
 * 
 * Example:
 * {@code
 * public class WeatherAgent extends Agent {
 *     @FunctionSpec(description = "Get weather for location")
 *     public Result getWeather(
 *         @Parameter(description = "City name") String city,
 *         Map<String, Object> context) {
 *         // Implementation with access to context
 *     }
 * }
 * }
 */
public abstract class Agent {
    
    /**
     * Returns the system prompt that defines this agent's role and capabilities.
     * 
     * The system prompt should:
     * 1. Define the agent's role and expertise
     * 2. List key capabilities and available tools
     * 3. Specify any constraints or guidelines
     * 
     * @param context The current execution context
     * @return A system prompt string
     */
    public String getSystemPrompt(Map<String, Object> context) {
        return "You are a helpful AI assistant with access to various tools and functions.\n\n" +
               "Guidelines:\n" +
               "1. Use available tools when needed to complete tasks\n" +
               "2. Provide clear, concise responses\n" +
               "3. Ask for clarification if a request is unclear\n" +
               "4. Respect user privacy and security\n\n" +
               "Always think step-by-step and use tools appropriately to achieve the best results.";
    }

    /**
     * Returns the tool choice mode for this agent.
     * 
     * Tool choice modes:
     * - AUTO: Agent automatically decides when to use tools
     * - NONE: Agent never uses tools
     * - REQUIRED: Agent must use a tool for each response
     * 
     * @return The tool choice mode
     */
    public ToolChoice getToolChoice() {
        return ToolChoice.AUTO;
    }

    /**
     * Discovers and returns all available tools from this agent using reflection.
     * 
     * Tools are discovered by scanning for methods annotated with @FunctionSpec.
     * For each annotated method:
     * 1. Name and description are extracted from @FunctionSpec
     * 2. Parameters are extracted from @Parameter annotations
     * 3. A tool specification is built for LLM consumption
     * 
     * @return List of tool specifications
     */
    public List<Map<String, Object>> getTools() {
        List<Map<String, Object>> tools = new ArrayList<>();

        for (Method method : this.getClass().getMethods()) {
            FunctionSpec spec = method.getAnnotation(FunctionSpec.class);
            if (spec != null) {
                Map<String, Object> tool = new HashMap<>();
                tool.put("type", "function");
                tool.put("function", buildFunctionSpec(method));
                tools.add(tool);
            }
        }

        return tools;
    }

    /**
     * Build function specification from method annotations.
     */
    public Map<String, Object> buildFunctionSpec(Method method) {
        FunctionSpec spec = method.getAnnotation(FunctionSpec.class);
        if (spec == null) {
            throw new IllegalArgumentException("Method " + method.getName() + " is not annotated with @FunctionSpec");
        }

        Map<String, Object> function = new HashMap<>();
        function.put("name", method.getName());
        function.put("description", spec.description());

        // Build parameters object
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");

        // Get parameter properties
        Map<String, Object> properties = new HashMap<>();
        List<String> required = new ArrayList<>();

        // Get parameters from method reflection
        java.lang.reflect.Parameter[] methodParams = method.getParameters();
        for (java.lang.reflect.Parameter methodParam : methodParams) {
            // Skip context parameter
            if (methodParam.getType().equals(Map.class)) {
                continue;
            }

            Parameter param = methodParam.getAnnotation(Parameter.class);
            if (param != null) {
                Map<String, Object> paramSpec = new HashMap<>();
                paramSpec.put("type", getJsonType(methodParam.getType()));
                paramSpec.put("description", param.description());

                // Add default value if specified
                if (!param.defaultValue().isEmpty()) {
                    paramSpec.put("default", param.defaultValue());
                } else {
                    // If no default value, parameter is required
                    required.add(methodParam.getName());
                }

                properties.put(methodParam.getName(), paramSpec);
            }
        }

        parameters.put("properties", properties);
        if (!required.isEmpty()) {
            parameters.put("required", required);
        }
        function.put("parameters", parameters);

        return function;
    }

    private String getJsonType(Class<?> type) {
        if (type.equals(String.class)) {
            return "string";
        } else if (type.equals(Integer.class) || type.equals(int.class) 
                || type.equals(Long.class) || type.equals(long.class)) {
            return "integer";
        } else if (type.equals(Double.class) || type.equals(double.class) 
                || type.equals(Float.class) || type.equals(float.class)) {
            return "number";
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return "boolean";
        } else {
            return "string"; // Default to string for unknown types
        }
    }

    /**
     * Find a function by name and get its specification.
     */
    public Map<String, Object> getFunctionSpec(String name) {
        for (Method method : this.getClass().getMethods()) {
            FunctionSpec spec = method.getAnnotation(FunctionSpec.class);
            if (spec != null && method.getName().equals(name)) {
                return buildFunctionSpec(method);
            }
        }
        return null;
    }

    /**
     * Find a function method by name.
     */
    public Method findFunction(String name) {
        for (Method method : this.getClass().getMethods()) {
            FunctionSpec spec = method.getAnnotation(FunctionSpec.class);
            if (spec != null && method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Get parameter information for a function.
     */
    public List<Parameter> getFunctionParameters(String name) {
        Method method = findFunction(name);
        if (method != null) {
            List<Parameter> params = new ArrayList<>();
            for (java.lang.reflect.Parameter methodParam : method.getParameters()) {
                Parameter param = methodParam.getAnnotation(Parameter.class);
                if (param != null) {
                    params.add(param);
                }
            }
            return params;
        }
        return Collections.emptyList();
    }
}
