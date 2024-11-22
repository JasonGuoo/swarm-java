package org.icespace.swarm.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.icespace.swarm.llm.model.ChatRequest;
import org.icespace.swarm.llm.model.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

/**
 * OpenAI API client implementation.
 * Supports all OpenAI Chat API features:
 * - Chat completions (GPT-3.5, GPT-4)
 * - Stream mode for real-time responses
 * - Function/tool calling
 * - Response formatting
 * - Token usage tracking
 *
 * Features:
 * - Full OpenAI Chat API compatibility
 * - Automatic request/response formatting
 * - Error handling with detailed messages
 * - Stream support for real-time responses
 * - Token usage tracking
 *
 * Configuration:
 * - API key required (from OpenAI dashboard)
 * - Model selection (e.g., gpt-3.5-turbo, gpt-4)
 * - Base URL (api.openai.com by default)
 *
 * Example usage:
 * <pre>{@code
 * OpenAIClient client = new OpenAIClient(
 *     "your-api-key",
 *     "gpt-4"
 * );
 *
 * ChatResponse response = client.chat(ChatRequest.builder()
 *     .messages(messages)
 *     .tools(tools)
 *     .build());
 * }</pre>
 */
public class OpenAIClient extends BaseLLMClient {
    private static final Logger log = LoggerFactory.getLogger(OpenAIClient.class);
    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    private static final String CHAT_ENDPOINT = "/chat/completions";

    public OpenAIClient(String baseUrl, String apiKey, String model) {
        super(baseUrl, apiKey, model);
    }

    @Override
    public ChatResponse chat(ChatRequest request) throws LLMException {
        try {
            // Set model if not already set
            if (request.getModel() == null || request.getModel().trim().isEmpty()) {
                request.setModel(model);
                log.debug("Setting default model: {}", model);
            }

            String jsonBody = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + CHAT_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new LLMException("API call failed with status " + response.statusCode()
                        + ": " + response.body());
            }

            return objectMapper.readValue(response.body(), ChatResponse.class);

        } catch (Exception e) {
            throw new LLMException("Failed to get chat completion", e);
        }
    }

    @Override
    public Stream<ChatResponse> stream(ChatRequest request) throws LLMException {
        try {
            // Set model if not already set
            if (request.getModel() == null || request.getModel().trim().isEmpty()) {
                request.setModel(model);
                log.debug("Setting default model: {}", model);
            }

            // Enable streaming
            request.setStream(true);

            String jsonBody = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + CHAT_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Use BodyHandlers.ofLines() for streaming
            HttpResponse<Stream<String>> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofLines());

            if (response.statusCode() != 200) {
                throw new LLMException("API call failed with status " + response.statusCode());
            }

            return response.body()
                    .filter(line -> !line.isEmpty())
                    .filter(line -> line.startsWith("data: "))
                    .map(line -> line.substring(6)) // Remove "data: " prefix
                    .filter(line -> !line.equals("[DONE]"))
                    .map(line -> {
                        try {
                            return objectMapper.readValue(line, ChatResponse.class);
                        } catch (Exception e) {
                            log.error("Failed to parse streaming response: {}", line, e);
                            throw new RuntimeException(e);
                        }
                    });

        } catch (Exception e) {
            throw new LLMException("Failed to get streaming chat completion", e);
        }
    }

    @Override
    public void validateConnection() throws LLMException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/models"))
                    .header("Authorization", "Bearer " + apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new LLMException("Failed to validate OpenAI connection: "
                        + response.statusCode());
            }
        } catch (Exception e) {
            throw new LLMException("Failed to validate OpenAI connection", e);
        }
    }
}