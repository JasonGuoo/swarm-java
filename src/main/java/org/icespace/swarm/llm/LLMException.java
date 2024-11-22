package org.icespace.swarm.llm;

/**
 * Exception thrown by LLM clients when errors occur.
 * This exception wraps provider-specific errors and provides
 * a consistent way to handle:
 * - API errors (invalid key, rate limits)
 * - Network errors (timeout, connection failed)
 * - Response parsing errors
 * - Invalid request errors
 *
 * The exception includes:
 * - A descriptive message
 * - The original cause (if any)
 * - Provider-specific error details (when available)
 */
public class LLMException extends RuntimeException {
    public LLMException(String message) {
        super(message);
    }

    public LLMException(String message, Throwable cause) {
        super(message, cause);
    }
}