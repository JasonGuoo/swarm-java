package org.icespace.swarm.llm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tracks token usage in LLM requests and responses.
 * This information is useful for:
 * - Monitoring API costs
 * - Staying within token limits
 * - Optimizing prompt engineering
 *
 * The class tracks:
 * - Prompt tokens (input)
 * - Completion tokens (output)
 * - Total tokens (prompt + completion)
 *
 * Example usage:
 * <pre>{@code
 * Usage usage = response.getUsage();
 * int totalCost = usage.getTotalTokens();
 * int inputSize = usage.getPromptTokens();
 * int outputSize = usage.getCompletionTokens();
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Usage {
    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    @JsonProperty("total_tokens")
    private Integer totalTokens;

    private JsonNode rawJson;
}