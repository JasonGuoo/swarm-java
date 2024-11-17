package org.icespace.swarm.llm.model.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.icespace.swarm.llm.model.ChatResponse;

public class ChatResponseDeserializer extends JsonDeserializer<ChatResponse> {
    @Override
    public ChatResponse deserialize(JsonParser p, DeserializationContext ctxt) throws JsonProcessingException {
        try {
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            JsonNode node = mapper.readTree(p);

            // First deserialize normally
            ChatResponse response = mapper.treeToValue(node, ChatResponse.class);

            // Then store the raw JSON
            response.setRawJson(node);

            // Store raw JSON for nested objects too
            if (response.getChoices() != null) {
                for (int i = 0; i < response.getChoices().size(); i++) {
                    response.getChoices().get(i).setRawJson(node.get("choices").get(i));
                }
            }

            if (response.getUsage() != null) {
                response.getUsage().setRawJson(node.get("usage"));
            }

            return response;
        } catch (Exception e) {
            // Convert any checked exceptions to JsonProcessingException
            throw new JsonProcessingException("Failed to deserialize ChatResponse", e) {
            };
        }
    }
}