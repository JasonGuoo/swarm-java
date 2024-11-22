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
 * Represents a message in a chat conversation with an LLM.
 * Messages can be:
 * - User messages (role="user")
 * - Assistant messages (role="assistant")
 * - System messages (role="system")
 * - Tool/function messages (role="tool" or "function")
 *
 * Features:
 * - Standard message fields (role, content)
 * - Tool/function calling support (tool_calls)
 * - Optional name field for multi-user chats
 * - Raw JSON preservation
 * - Dynamic field access
 *
 * Example usage:
 * <pre>{@code
 * // Create a user message
 * Message userMsg = Message.builder()
 *     .role("user")
 *     .content("What's the weather?")
 *     .build();
 *
 * // Create an assistant message with tool calls
 * Message assistantMsg = Message.builder()
 *     .role("assistant")
 *     .toolCalls(new ToolCall[]{weatherToolCall})
 *     .build();
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {
    @JsonProperty("role")
    private String role;

    @JsonProperty("content")
    private String content;

    @JsonProperty("name")
    private String name;

    @JsonProperty("tool_name")
    private String toolName;

    @JsonProperty("tool_call_id")
    private String toolCallId;

    @JsonProperty("tool_calls")
    private ToolCall[] toolCalls;

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