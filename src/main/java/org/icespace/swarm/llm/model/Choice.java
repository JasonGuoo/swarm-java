package org.icespace.swarm.llm.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a single choice/completion from an LLM response.
 * Each choice contains:
 * - An index (for multiple completions)
 * - The message content
 * - A finish reason indicating why the LLM stopped generating
 *
 * The class also preserves raw JSON and supports dynamic field access
 * for provider-specific extensions.
 *
 * Example usage:
 * <pre>{@code
 * Choice choice = response.getChoices().get(0);
 * String content = choice.getMessage().getContent();
 * String reason = choice.getFinishReason();
 *
 * // Access provider-specific fields
 * Object customField = choice.getFieldValue("custom/field/path");
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Choice {
    @JsonProperty("index")
    private Integer index;

    @JsonProperty("message")
    private Message message;

    @JsonProperty("finish_reason")
    private String finishReason;

    @JsonIgnore
    private JsonNode rawJson;

    @JsonAnySetter
    private Map<String, Object> additionalProperties;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Get a field value from the raw JSON with automatic type casting
     */
    public Object getFieldValue(String fieldPath) {
        if (rawJson == null || fieldPath == null) {
            return null;
        }

        // Remove leading slash if present
        if (fieldPath.startsWith("/")) {
            fieldPath = fieldPath.substring(1);
        }

        // Split the path and traverse the JSON tree
        String[] pathParts = fieldPath.split("/");
        JsonNode current = rawJson;

        for (String part : pathParts) {
            if (current == null || current.isMissingNode()) {
                return null;
            }

            // Handle array indices
            if (part.matches("\\d+")) {
                int index = Integer.parseInt(part);
                if (!current.isArray() || index >= current.size()) {
                    return null;
                }
                current = current.get(index);
            } else {
                current = current.get(part);
            }
        }

        return convertJsonNodeToValue(current);
    }

    /**
     * Get a field value with explicit type casting
     */
    public <T> T getFieldValue(String fieldPath, Class<T> type) {
        Object value = getFieldValue(fieldPath);
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(value, type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Object convertJsonNodeToValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        } else if (node.isTextual()) {
            return node.asText();
        } else if (node.isNumber()) {
            return node.numberValue();
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isArray()) {
            return objectMapper.convertValue(node, Object[].class);
        } else if (node.isObject()) {
            return objectMapper.convertValue(node, Map.class);
        }
        return null;
    }
}