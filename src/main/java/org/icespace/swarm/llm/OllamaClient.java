package org.icespace.swarm.llm;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;

import org.icespace.swarm.llm.model.ChatRequest;
import org.icespace.swarm.llm.model.ChatResponse;
import org.icespace.swarm.llm.model.Message;
import org.icespace.swarm.llm.model.Choice;
import org.icespace.swarm.llm.model.Usage;

/**
 * Ollama API client implementation.
 * Extends OpenAI client since Ollama follows a similar API format.
 */
public class OllamaClient extends OpenAIClient {
    private boolean modelValidated = false;

    public OllamaClient(String model) {
        super("http://localhost:11434/api", "", model);
    }

    public OllamaClient(String baseUrl, String model) {
        super(baseUrl, "", model);
    }

    @Override
    protected String getEndpointUrl() {
        return baseUrl + "/chat";
    }

    @Override
    protected Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }

    @Override
    public ChatResponse chat(ChatRequest request) throws LLMException {
        // Lazy validation of model on first use
        if (!modelValidated) {
            validateModelAvailability(this.model);
            modelValidated = true;
        }

        // Convert model name if it's an OpenAI model name
        String modelName = request.getModel();
        if (modelName != null) {
            modelName = convertModelName(modelName);
            request = ChatRequest.builder()
                    .model(modelName)
                    .messages(request.getMessages())
                    .temperature(request.getTemperature())
                    .topP(request.getTopP())
                    .maxTokens(request.getMaxTokens())
                    .stream(false) // Explicitly disable streaming
                    .build();
        }

        try {
            String requestBody = objectMapper.writeValueAsString(request);
            System.out.println("Ollama Request: " + requestBody);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getEndpointUrl()))
                    .headers(getHeadersArray())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println("Ollama Response Status: " + response.statusCode());
            System.out.println("Ollama Response Body: " + response.body());

            if (response.statusCode() != 200) {
                throw new LLMException("Ollama API error: " + response.body());
            }

            // Parse Ollama's response format
            JsonNode responseNode = objectMapper.readTree(response.body());

            // Convert Ollama response to standard ChatResponse format
            return ChatResponse.builder()
                    .model(responseNode.get("model").asText())
                    .created(responseNode.get("created_at") != null
                            ? Instant.parse(responseNode.get("created_at").asText()).toEpochMilli()
                            : null)
                    .choices(Collections.singletonList(
                            Choice.builder()
                                    .message(Message.builder()
                                            .role(responseNode.get("message").get("role").asText())
                                            .content(responseNode.get("message").get("content").asText())
                                            .build())
                                    .finishReason(responseNode.get("done_reason").asText())
                                    .build()))
                    .usage(Usage.builder()
                            .promptTokens(responseNode.get("prompt_eval_count").asInt())
                            .completionTokens(responseNode.get("eval_count").asInt())
                            .totalTokens(responseNode.get("prompt_eval_count").asInt() +
                                    responseNode.get("eval_count").asInt())
                            .build())
                    .rawJson(responseNode) // Store the original response
                    .build();

        } catch (Exception e) {
            throw new LLMException("Failed to complete chat request", e);
        }
    }

    private String convertModelName(String model) {
        if (model == null) {
            return null;
        }
        String modelLower = model.toLowerCase();
        if ("gpt-3.5-turbo".equals(modelLower) || "gpt-4".equals(modelLower)) {
            return "llama2";
        }
        return model;
    }

    @Override
    protected String validateModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model name is required");
        }
        return model.trim();
    }

    private void validateModelAvailability(String model) throws LLMException {
        try {
            ChatRequest request = ChatRequest.builder()
                    .model(model)
                    .messages(Collections.singletonList(
                            Message.builder()
                                    .role("user")
                                    .content("test")
                                    .build()))
                    .maxTokens(1)
                    .stream(false) // Explicitly disable streaming
                    .build();

            // Call parent's chat to avoid recursive validation
            super.chat(request);
        } catch (Exception e) {
            throw new LLMException("Failed to validate Ollama model: " + model +
                    ". Make sure the model is pulled", e);
        }
    }

    @Override
    public void validateConnection() throws LLMException {
        try {
            validateModelAvailability(this.model);
            modelValidated = true;
        } catch (Exception e) {
            throw new LLMException("Failed to validate Ollama connection. " +
                    "Make sure Ollama is running locally", e);
        }
    }

    public String getModel() {
        return model;
    }
}