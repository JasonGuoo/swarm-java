package org.icespace.swarm.llm;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import org.icespace.swarm.llm.model.ChatRequest;
import org.icespace.swarm.llm.model.ChatResponse;
import org.icespace.swarm.llm.model.Message;

/**
 * ChatGLM API client implementation.
 * Extends OpenAI client since ChatGLM follows OpenAI's API format.
 */
public class ChatGLMClient extends OpenAIClient {
    private static final String DEFAULT_BASE_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String DEFAULT_MODEL = "glm-4-flash";

    public ChatGLMClient(String apiKey) {
        super(DEFAULT_BASE_URL, apiKey, DEFAULT_MODEL);
    }

    public ChatGLMClient(String apiKey, String model) {
        super(DEFAULT_BASE_URL, apiKey, model != null ? model : DEFAULT_MODEL);
    }

    public ChatGLMClient(String baseUrl, String apiKey, String model) {
        super(baseUrl, apiKey, model != null ? model : DEFAULT_MODEL);
    }

    @Override
    protected String getEndpointUrl() {
        return baseUrl; // Use the full URL directly
    }

    @Override
    protected Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    @Override
    public ChatResponse chat(ChatRequest request) throws LLMException {
        // Convert model name if it's an OpenAI model name
        String modelName = request.getModel();
        if (modelName != null) {
            modelName = convertModelName(modelName);
        } else {
            modelName = this.model; // Use the client's model if request doesn't specify one
        }

        request = ChatRequest.builder()
                .model(modelName)
                .messages(request.getMessages())
                .temperature(request.getTemperature())
                .topP(request.getTopP())
                .maxTokens(request.getMaxTokens())
                .build();

        try {
            // Print request for debugging
            System.out.println("ChatGLM Request: " + objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(request));

            ChatResponse response = super.chat(request);

            // Print response for debugging
            System.out.println("ChatGLM Response: " + objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(response));

            return response;
        } catch (Exception e) {
            throw new LLMException("Failed to complete chat request", e);
        }
    }

    private String convertModelName(String model) {
        if (model == null) {
            return DEFAULT_MODEL;
        }
        String modelLower = model.toLowerCase();
        if ("gpt-3.5-turbo".equals(modelLower)) {
            return "chatglm_turbo";
        } else if ("gpt-4".equals(modelLower)) {
            return "chatglm_pro";
        }
        return model;
    }

    @Override
    protected String validateModel(String model) {
        // Allow null model, will use default
        if (model == null) {
            return DEFAULT_MODEL;
        }
        return model.trim();
    }

    @Override
    public void validateConnection() throws LLMException {
        try {
            ChatRequest request = ChatRequest.builder()
                    .model(this.model)
                    .messages(Collections.singletonList(
                            Message.builder()
                                    .role("user")
                                    .content("test")
                                    .build()))
                    .maxTokens(1)
                    .build();

            chat(request);
        } catch (Exception e) {
            throw new LLMException("Failed to validate ChatGLM connection", e);
        }
    }

    public String getModel() {
        return model;
    }
}