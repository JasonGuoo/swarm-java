package org.icespace.swarm.llm.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatResponseDeserializer extends JsonDeserializer<ChatResponse> {
    @Override
    public ChatResponse deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        ChatResponse response = new ChatResponse();

        // Manually deserialize the standard fields
        if (node.has("id")) {
            response.setId(node.get("id").asText());
        }
        if (node.has("object")) {
            response.setObject(node.get("object").asText());
        }
        if (node.has("created")) {
            response.setCreated(node.get("created").asLong());
        }
        if (node.has("model")) {
            response.setModel(node.get("model").asText());
        }
        
        // Handle choices array
        if (node.has("choices")) {
            JsonNode choices = node.get("choices");
            if (choices.isArray()) {
                List<Choice> choiceList = new ArrayList<>();
                for (JsonNode choice : choices) {
                    Choice c = mapper.treeToValue(choice, Choice.class);
                    choiceList.add(c);
                }
                response.setChoices(choiceList);
            }
        }
        
        // Handle usage object
        if (node.has("usage")) {
            Usage usage = mapper.treeToValue(node.get("usage"), Usage.class);
            response.setUsage(usage);
        }
        
        // Store the complete JSON for custom field access
        response.setRawJson(node);
        
        return response;
    }
}
