package org.icespace.swarm.llm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a function call within a tool call.
 * Contains the name of the function to call and its arguments
 * as a JSON string.
 *
 * This class follows the OpenAI Chat API format for function calls:
 * {
 *   "name": "get_weather",
 *   "arguments": "{\"location\":\"Boston, MA\"}"
 * }
 *
 * The arguments field is a JSON string that should be parsed
 * according to the function's parameter schema.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FunctionCall {
    @JsonProperty("name")
    private String name;

    @JsonProperty("arguments")
    private String arguments;
}