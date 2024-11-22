package org.icespace.swarm.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.icespace.swarm.llm.model.ChatRequest;
import org.icespace.swarm.llm.model.ChatResponse;
import org.icespace.swarm.llm.model.Message;
import org.icespace.swarm.llm.model.Choice;
import org.icespace.swarm.llm.model.Usage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * ChatGLM API client implementation.
 * Provides access to Zhipu AI's ChatGLM models:
 * - ChatGLM-6B
 * - ChatGLM-130B
 * - GLM-4
 *
 * Features:
 * - OpenAI-compatible API format
 * - Chinese language optimization
 * - Function/tool calling support
 * - Stream mode for real-time responses
 * - Token usage tracking
 *
 * Configuration:
 * - API key (from Zhipu AI dashboard)
 * - Model selection
 * - Base URL (api.zhipu.ai by default)
 *
 * Example usage:
 * <pre>{@code
 * ChatGLMClient client = new ChatGLMClient(
 *     "your-api-key",
 *     "chatglm_pro"
 * );
 *
 * ChatResponse response = client.chat(ChatRequest.builder()
 *     .messages(messages)
 *     .tools(tools)
 *     .build());
 * }</pre>
 *
 * Note: Some features may vary from OpenAI's implementation
 */
public class ChatGLMClient extends BaseLLMClient {
    private static final String DEFAULT_BASE_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String DEFAULT_MODEL = "glm-4-flash";
    private final String apiKey;

    public ChatGLMClient(String apiKey) {
        super(DEFAULT_BASE_URL, apiKey, DEFAULT_MODEL);
        this.apiKey = apiKey;
    }

    public ChatGLMClient(String baseUrl, String apiKey, String model) {
        super(baseUrl, apiKey, model);
        this.apiKey = apiKey;
    }

    protected Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    @Override
    public ChatResponse chat(ChatRequest request) throws LLMException {
        try {
            // Create request body with model field
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model); // Use model from constructor
            requestBody.set("messages", objectMapper.valueToTree(request.getMessages()));
            
            // Add tools if present
            if (request.getTools() != null && !request.getTools().isEmpty()) {
                requestBody.set("tools", objectMapper.valueToTree(request.getTools()));
            }
            
            // Add tool_choice if present
            if (request.getFunctionCall() != null) {
                if (request.getFunctionCall() instanceof String) {
                    requestBody.put("tool_choice", (String) request.getFunctionCall());
                } else {
                    requestBody.set("tool_choice", objectMapper.valueToTree(request.getFunctionCall()));
                }
            }
            
            // Add other parameters if present
            if (request.getTemperature() != null) {
                requestBody.put("temperature", request.getTemperature());
            }
            if (request.getTopP() != null) {
                requestBody.put("top_p", request.getTopP());
            }
            if (request.getMaxTokens() != null) {
                requestBody.put("max_tokens", request.getMaxTokens());
            }

            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .headers("Content-Type", "application/json")
                    .headers("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() >= 400) {
                throw new LLMException("ChatGLM API error: " + response.body());
            }

            return objectMapper.readValue(response.body(), ChatResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new LLMException("Failed to send chat request: " + e.getMessage());
        }
    }

    @Override
    public void validateConnection() throws LLMException {
        try {
            ChatRequest request = ChatRequest.builder()
                    .messages(Collections.singletonList(
                            Message.builder()
                                    .role("user")
                                    .content("test")
                                    .build()))
                    .maxTokens(1)
                    .build();

            chat(request);
        } catch (Exception e) {
            throw new LLMException("Failed to validate ChatGLM connection: " + e.getMessage());
        }
    }

    @Override
    public Stream<ChatResponse> stream(ChatRequest request) throws LLMException {
        try {
            // Set model if not already set
            if (request.getModel() == null || request.getModel().trim().isEmpty()) {
                request.setModel(model);
            }

            // Enable streaming
            request.setStream(true);

            String jsonBody = objectMapper.writeValueAsString(request);
            Map<String, String> headers = getHeaders();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .headers(headers.entrySet().stream()
                            .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                            .toArray(String[]::new))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<Stream<String>> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofLines());

            if (response.statusCode() != 200) {
                throw new LLMException("ChatGLM API call failed with status " + response.statusCode());
            }

            return response.body()
                    .filter(line -> !line.isEmpty())
                    .map(line -> {
                        try {
                            JsonNode node = objectMapper.readTree(line);
                            if (node.has("error")) {
                                throw new LLMException("ChatGLM API error: " + node.get("error").asText());
                            }
                            return objectMapper.treeToValue(node, ChatResponse.class);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse ChatGLM streaming response", e);
                        }
                    });

        } catch (Exception e) {
            throw new LLMException("Failed to get streaming chat completion from ChatGLM", e);
        }
    }

    public String getModel() {
        return model;
    }
}