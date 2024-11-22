package org.icespace.swarm.llm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents the schema of a function that can be called by the LLM.
 * This schema describes the function's:
 * - Name
 * - Description
 * - Parameters (type, properties, required fields)
 *
 * The schema follows the OpenAI Chat API format and JSON Schema spec:
 * {
 *   "name": "get_weather",
 *   "description": "Get the weather in a location",
 *   "parameters": {
 *     "type": "object",
 *     "properties": {
 *       "location": {
 *         "type": "string",
 *         "description": "City and state (e.g. San Francisco, CA)"
 *       }
 *     },
 *     "required": ["location"]
 *   }
 * }
 *
 * This schema helps the LLM understand:
 * - When to call the function
 * - What parameters are needed
 * - What each parameter means
 * - Which parameters are required vs optional
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FunctionSchema {
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    // Inner class for parameters structure if needed
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Parameters {
        private String type;
        private Map<String, Object> properties;
        private List<String> required;
    }
}