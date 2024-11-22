package org.icespace.swarm.llm;

import org.icespace.swarm.llm.model.ChatRequest;
import org.icespace.swarm.llm.model.ChatResponse;

import java.util.stream.Stream;

/**
 * Interface for interacting with Language Learning Models (LLMs).
 * Provides a unified way to:
 * - Send chat completions requests
 * - Receive responses
 * - Stream responses
 * - Handle errors
 *
 * This interface abstracts away the differences between various LLM providers
 * (OpenAI, ChatGLM, Ollama, etc.) and provides a consistent API for:
 * - Basic chat completions
 * - Streaming responses
 * - Function/tool calling
 * - Error handling
 *
 * Implementations should handle:
 * - API authentication
 * - Request formatting
 * - Response parsing
 * - Rate limiting
 * - Error recovery
 */
public interface LLMClient {
    /**
     * Send a chat request to the LLM service
     * 
     * @param request The chat request
     * @return The chat response
     * @throws LLMException if the request fails
     */
    ChatResponse chat(ChatRequest request) throws LLMException;

    /**
     * Send a streaming chat request to the LLM service
     * 
     * @param request The chat request
     * @return A stream of chat responses
     * @throws LLMException if the request fails
     */
    Stream<ChatResponse> stream(ChatRequest request) throws LLMException;

    /**
     * Check if the client is properly configured and can connect to the service
     * 
     * @throws LLMException if the connection test fails
     */
    void validateConnection() throws LLMException;
}