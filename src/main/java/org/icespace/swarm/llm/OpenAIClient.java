package org.icespace.swarm.llm;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.icespace.swarm.llm.model.ChatRequest;
import org.icespace.swarm.llm.model.ChatResponse;
import org.icespace.swarm.llm.model.Message;

/**
 * OpenAI API client implementation.
 */
public class OpenAIClient extends BaseLLMClient {
    protected final String model;

    public OpenAIClient(String apiKey, String model) {
        super("https://api.openai.com/v1", apiKey);
        this.model = validateModel(model);
    }

    public OpenAIClient(String baseUrl, String apiKey, String model) {
        super(baseUrl, apiKey);
        this.model = validateModel(model);
    }

    protected String validateModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model name is required");
        }
        return model.trim();
    }

    protected String getEndpointUrl() {
        return baseUrl + "/chat/completions";
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
            String requestBody = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getEndpointUrl()))
                    .headers(getHeadersArray())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new LLMException("OpenAI API error: " + response.body());
            }

            return objectMapper.readValue(response.body(), ChatResponse.class);
        } catch (Exception e) {
            if (e instanceof LLMException) {
                throw (LLMException) e;
            }
            throw new LLMException("Failed to complete chat request", e);
        }
    }

    protected String[] getHeadersArray() {
        Map<String, String> headers = getHeaders();
        String[] headerArray = new String[headers.size() * 2];
        int i = 0;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headerArray[i++] = entry.getKey();
            headerArray[i++] = entry.getValue();
        }
        return headerArray;
    }

    @Override
    public void validateConnection() throws LLMException {
        try {
            ChatRequest request = ChatRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(Collections.singletonList(
                            Message.builder()
                                    .role("user")
                                    .content("test")
                                    .build()))
                    .maxTokens(1)
                    .build();

            chat(request);
        } catch (Exception e) {
            throw new LLMException("Failed to validate OpenAI connection", e);
        }
    }
}