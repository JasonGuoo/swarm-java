package org.icespace.swarm.llm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a tool call made by the LLM in a response.
 * Currently supports function-type tool calls, where the LLM requests
 * to call a specific function with arguments.
 *
 * This class follows the OpenAI Chat API format for tool calls, with additional
 * fields supported by different LLM providers:
 * {
 *   "id": "call_xyz",
 *   "type": "function",
 *   "index": 0,
 *   "function": {
 *     "name": "get_weather",
 *     "arguments": "{\"location\":\"Boston, MA\"}"
 *   }
 * }
 *
 * The tool call appears in assistant messages when the LLM decides
 * to use a tool/function to help answer the user's query.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolCall {
    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("index")
    private Integer index;

    @JsonProperty("function")
    private FunctionCall function;
}