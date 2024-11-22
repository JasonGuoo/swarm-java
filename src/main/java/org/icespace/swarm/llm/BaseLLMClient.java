package org.icespace.swarm.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.icespace.swarm.llm.model.ChatRequest;
import org.icespace.swarm.llm.model.ChatResponse;

import java.net.http.HttpClient;
import java.util.stream.Stream;

/**
 * Base implementation of LLMClient interface.
 * Provides common functionality for LLM clients:
 * - JSON serialization/deserialization
 * - Error handling
 * - Stream processing
 *
 * Specific LLM clients (OpenAI, ChatGLM, etc.) should extend this class
 * and implement the provider-specific details:
 * - API endpoint URLs
 * - Authentication
 * - Request formatting
 * - Response parsing
 * - Error mapping
 *
 * The class uses Jackson for JSON processing and provides
 * a shared ObjectMapper instance for consistent JSON handling.
 */
public abstract class BaseLLMClient implements LLMClient {
    protected final String baseUrl;
    protected final String apiKey;
    protected final String model;
    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;

    protected BaseLLMClient(String baseUrl, String apiKey, String model) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.model = model;
    }

    @Override
    public Stream<ChatResponse> stream(ChatRequest request) throws LLMException {
        throw new UnsupportedOperationException("Streaming not supported by this LLM provider");
    }
}