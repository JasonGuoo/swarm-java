package org.icespace.swarm.llm.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a request to an LLM service (e.g., OpenAI, ChatGLM, Ollama).
 * This class follows the OpenAI Chat API format, with support for:
 * - Basic chat parameters (model, messages, temperature, etc.)
 * - Tool/function calling (tools, tool_choice)
 * - Streaming responses
 * - Response formatting (n, max_tokens)
 * - Response quality control (presence_penalty, frequency_penalty)
 *
 * Example usage:
 * <pre>{@code
 * ChatRequest request = ChatRequest.builder()
 *     .model("gpt-4")
 *     .messages(Arrays.asList(new Message("user", "Hello!")))
 *     .tools(Arrays.asList(weatherTool))
 *     .toolChoice("auto")
 *     .build();
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRequest {
    @JsonProperty("model")
    private String model;

    @JsonProperty("messages")
    private List<Message> messages;

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("n")
    private Integer n;

    @JsonProperty("stream")
    private Boolean stream;

    @JsonProperty("stop")
    private List<String> stop;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @JsonProperty("presence_penalty")
    private Double presencePenalty;

    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;

    @JsonProperty("logit_bias")
    private Map<String, Integer> logitBias;

    @JsonProperty("user")
    private String user;

    @JsonProperty("tools")
    private List<Tool> tools;

    @JsonProperty("tool_choice")
    private Object toolChoice;

    // For backward compatibility
    @JsonIgnore
    public List<FunctionSchema> getFunctions() {
        if (tools == null) return null;
        return tools.stream()
                .map(Tool::getFunction)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public void setFunctions(List<FunctionSchema> functions) {
        if (functions == null) {
            this.tools = null;
        } else {
            this.tools = functions.stream()
                    .map(Tool::fromFunctionSchema)
                    .collect(Collectors.toList());
        }
    }

    @JsonIgnore
    public Object getFunctionCall() {
        return toolChoice;
    }

    @JsonIgnore
    public void setFunctionCall(Object functionCall) {
        this.toolChoice = convertFunctionCallToToolChoice(functionCall);
    }

    private Object convertFunctionCallToToolChoice(Object functionCall) {
        if (functionCall == null) return null;
        if (functionCall instanceof String && functionCall.equals("auto")) {
            return "auto";
        }
        if (functionCall instanceof Map) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode toolChoice = mapper.createObjectNode();
            toolChoice.put("type", "function");
            toolChoice.set("function", mapper.valueToTree(functionCall));
            return toolChoice;
        }
        return functionCall;
    }
}