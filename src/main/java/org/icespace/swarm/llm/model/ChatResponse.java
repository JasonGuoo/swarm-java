package org.icespace.swarm.llm.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.*;

/**
 * Represents a response from an LLM service.
 * This class provides a flexible structure that can handle responses from various LLM providers
 * while maintaining compatibility with the OpenAI Chat API format.
 *
 * Features:
 * - Standard response fields (id, model, choices, etc.)
 * - Raw JSON preservation for provider-specific fields
 * - Dynamic field access through JSON path
 * - Automatic type conversion for field values
 * - Token usage tracking
 *
 * The class uses a custom deserializer to:
 * - Preserve the complete raw JSON response
 * - Handle dynamic/unknown fields
 * - Support nested field access
 * - Maintain type safety where possible
 *
 * Example usage:
 * <pre>{@code
 * // Basic field access
 * String model = response.getModel();
 * List<Choice> choices = response.getChoices();
 *
 * // Dynamic field access
 * String value = response.getFieldValue("choices/0/message/content", String.class);
 * Map<String, Object> usage = response.getFieldValue("usage", Map.class);
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = ChatResponse.ChatResponseDeserializer.class)
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

    /**
     * Custom deserializer to preserve raw JSON and handle dynamic fields
     */
    public static class ChatResponseDeserializer extends JsonDeserializer<ChatResponse> {
        @Override
        public ChatResponse deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            ChatResponse response = new ChatResponse();

            // Store raw JSON
            response.setRawJson(node.deepCopy());

            // Parse standard fields
            if (node.has("id")) response.setId(node.get("id").asText());
            if (node.has("object")) response.setObject(node.get("object").asText());
            if (node.has("created")) response.setCreated(node.get("created").asLong());
            if (node.has("model")) response.setModel(node.get("model").asText());

            // Parse choices
            if (node.has("choices")) {
                List<Choice> choices = new ArrayList<>();
                for (JsonNode choiceNode : node.get("choices")) {
                    Choice choice = objectMapper.treeToValue(choiceNode, Choice.class);
                    choice.setRawJson(choiceNode);
                    choices.add(choice);
                }
                response.setChoices(choices);
            }

            // Parse usage
            if (node.has("usage")) {
                response.setUsage(objectMapper.treeToValue(node.get("usage"), Usage.class));
            }

            // Store additional properties
            Map<String, Object> additionalProps = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                if (!Arrays.asList("id", "object", "created", "model", "choices", "usage").contains(key)) {
                    additionalProps.put(key, objectMapper.treeToValue(field.getValue(), Object.class));
                }
            }
            response.setAdditionalProperties(additionalProps);

            return response;
        }
    }
}