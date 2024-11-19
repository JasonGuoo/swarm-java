package org.icespace.swarm.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.icespace.swarm.llm.model.ChatRequest;
import org.icespace.swarm.llm.model.ChatResponse;

import java.net.http.HttpClient;

/**
 * Base implementation for LLM clients.
 * Provides common functionality while leaving provider-specific details to
 * subclasses.
 */
public abstract class BaseLLMClient implements LLMClient {
    protected final String baseUrl;
    protected final String apiKey;
    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;

    protected BaseLLMClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }
}