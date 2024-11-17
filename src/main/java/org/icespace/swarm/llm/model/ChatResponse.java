package org.icespace.swarm.llm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a response from a chat-based LLM API.
 * This class captures both standard response fields and any additional
 * provider-specific data.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = ChatResponseDeserializer.class)
public class ChatResponse {
    /** Unique identifier for the response */
    @JsonProperty("id")
    private String id;

    /** The type of object returned, usually "chat.completion" */
    @JsonProperty("object")
    private String object;

    /** Unix timestamp of when the response was created */
    @JsonProperty("created")
    private Long created;

    /** The model used to generate the response */
    @JsonProperty("model")
    private String model;

    /** List of generated completions */
    @JsonProperty("choices")
    private List<Choice> choices;

    /** Token usage information for the request and response */
    @JsonProperty("usage")
    private Usage usage;

    /**
     * Stores the complete original JSON response.
     * This allows access to fields that aren't mapped to class properties,
     * particularly useful for provider-specific extensions.
     */
    private JsonNode rawJson;

    /**
     * Retrieves a value from the raw JSON response using a JSON path.
     * 
     * @param fieldPath Path to the desired field (e.g.,
     *                  "choices/0/message/content")
     * @return The value at the specified path, or null if not found
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

        if (current == null || current.isNull()) {
            return null;
        }

        return convertJsonNodeToValue(current);
    }

    /**
     * Retrieves a typed value from the raw JSON response using a JSON path.
     * 
     * @param fieldPath Path to the desired field (e.g.,
     *                  "choices/0/message/content")
     * @param <T>       The type to convert the value to
     * @param valueType The class of the type to convert to
     * @return The value at the specified path converted to the specified type, or
     *         null if not found
     */
    public <T> T getFieldValue(String fieldPath, Class<T> valueType) {
        Object value = getFieldValue(fieldPath);
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(value, valueType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Object convertJsonNodeToValue(JsonNode node) {
        if (node.isTextual()) {
            return node.asText();
        } else if (node.isNumber()) {
            return node.numberValue();
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            node.elements().forEachRemaining(element -> list.add(convertJsonNodeToValue(element)));
            return list;
        } else if (node.isObject()) {
            Map<String, Object> map = new HashMap<>();
            node.fields().forEachRemaining(entry -> map.put(entry.getKey(), convertJsonNodeToValue(entry.getValue())));
            return map;
        }
        return null;
    }

    // Add convenience methods for common types
    public String getFieldValueAsString(String fieldPath) {
        return getFieldValue(fieldPath, String.class);
    }

    public Integer getFieldValueAsInteger(String fieldPath) {
        return getFieldValue(fieldPath, Integer.class);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();
}