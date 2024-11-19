package org.icespace.swarm.llm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResponse {
    @JsonProperty("id")
    private String id;

    @JsonProperty("object")
    private String object;

    @JsonProperty("created")
    private Long created;

    @JsonProperty("model")
    private String model;

    @JsonProperty("choices")
    private List<Choice> choices;

    @JsonProperty("usage")
    private Usage usage;

    private JsonNode rawJson;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get a field value from the raw JSON with automatic type casting
     * 
     * @param fieldPath JSON path to the field (e.g., "choices/0/message/content")
     * @return The value cast to its appropriate type, or null if not found
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
     * 
     * @param fieldPath JSON path to the field
     * @param type      Expected type class
     * @return The value cast to the specified type, or null if not found or cannot
     *         be cast
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
}