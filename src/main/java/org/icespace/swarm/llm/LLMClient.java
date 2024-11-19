package org.icespace.swarm.llm;

import org.icespace.swarm.llm.model.ChatRequest;
import org.icespace.swarm.llm.model.ChatResponse;

/**
 * Basic interface for LLM clients.
 * Callers are responsible for providing their own API keys and configurations.
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
     * Check if the client is properly configured and can connect to the service
     * 
     * @throws LLMException if the connection test fails
     */
    void validateConnection() throws LLMException;
}