package org.icespace.swarm.llm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a tool that can be used by the LLM.
 * Currently supports function-type tools, which allow the LLM to call
 * specific functions with structured parameters.
 *
 * This class follows the OpenAI Chat API format for tools:
 * {
 *   "type": "function",
 *   "function": {
 *     "name": "get_weather",
 *     "description": "Get weather in location",
 *     "parameters": {...}
 *   }
 * }
 *
 * Example usage:
 * <pre>{@code
 * Tool weatherTool = Tool.builder()
 *     .type("function")
 *     .function(weatherFunctionSchema)
 *     .build();
 *
 * // Or convert from a function schema
 * Tool tool = Tool.fromFunctionSchema(schema);
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tool {
    @JsonProperty("type")
    private String type;

    @JsonProperty("function")
    private FunctionSchema function;

    public static Tool fromFunctionSchema(FunctionSchema schema) {
        return Tool.builder()
                .type("function")
                .function(schema)
                .build();
    }
}
