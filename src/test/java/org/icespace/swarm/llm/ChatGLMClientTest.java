package org.icespace.swarm.llm;

import org.icespace.swarm.llm.model.ChatRequest;
import org.icespace.swarm.llm.model.ChatResponse;
import org.icespace.swarm.llm.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatGLMClientTest {
    private ChatGLMClient client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        String apiKey = TestEnv.require("CHATGLM_API_KEY");
        String model = TestEnv.get("CHATGLM_MODEL");
        client = new ChatGLMClient(apiKey, model);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testBasicChatRequest() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .model(client.getModel())
                .messages(List.of(
                        Message.builder()
                                .role("user")
                                .content("Hello! What is your name?")
                                .build()))
                .build();

        // Print request
        System.out.println("\n=== ChatGLM Basic Chat Request ===");
        System.out.println("Request: " + objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(request));

        ChatResponse response = client.chat(request);

        // Print response
        System.out.println("Response: " + objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(response));
        System.out.println("=====================================\n");

        assertNotNull(response);
        assertNotNull(response.getChoices());
        assertFalse(response.getChoices().isEmpty());
    }

    @Test
    void testWithNullModel() throws Exception {
        ChatGLMClient defaultClient = new ChatGLMClient(TestEnv.require("CHATGLM_API_KEY"), null);

        ChatRequest request = ChatRequest.builder()
                .messages(List.of(
                        Message.builder()
                                .role("user")
                                .content("Tell me a short joke.")
                                .build()))
                .build();

        // Print request
        System.out.println("\n=== ChatGLM Null Model Test ===");
        System.out.println("Request: " + objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(request));

        ChatResponse response = defaultClient.chat(request);

        // Print response
        System.out.println("Response: " + objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(response));
        System.out.println("=====================================\n");

        assertNotNull(response);
    }

    @Test
    void testModelConversion() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .model("gpt-3.5-turbo") // Should be converted to chatglm_turbo
                .messages(List.of(
                        Message.builder()
                                .role("user")
                                .content("Tell me a short joke.")
                                .build()))
                .build();

        // Print request
        System.out.println("\n=== ChatGLM Model Conversion Test ===");
        System.out.println("Request: " + objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(request));

        ChatResponse response = client.chat(request);

        // Print response
        System.out.println("Response: " + objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(response));
        System.out.println("=====================================\n");

        assertNotNull(response);
    }

    @Test
    void testWithParameters() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .model(client.getModel())
                .messages(List.of(
                        Message.builder()
                                .role("user")
                                .content("Write a very short poem about AI.")
                                .build()))
                .temperature(0.7)
                .maxTokens(100)
                .build();

        // Print request
        System.out.println("\n=== ChatGLM Parameters Test ===");
        System.out.println("Request: " + objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(request));

        ChatResponse response = client.chat(request);

        // Print response
        System.out.println("Response: " + objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(response));
        System.out.println("=====================================\n");

        assertNotNull(response);
    }

    @Test
    void testInvalidApiKey() {
        ChatGLMClient invalidClient = new ChatGLMClient("invalid-key", client.getModel());

        ChatRequest request = ChatRequest.builder()
                .model(client.getModel())
                .messages(List.of(
                        Message.builder()
                                .role("user")
                                .content("Hello!")
                                .build()))
                .build();

        // Print request
        System.out.println("\n=== ChatGLM Invalid API Key Test ===");
        try {
            System.out.println("Request: " + objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(request));

            ChatResponse response = invalidClient.chat(request);

            System.out.println("Response: " + objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(response));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println("=====================================\n");

        assertThrows(LLMException.class, () -> invalidClient.chat(request));
    }

    @Test
    void testConnectionValidation() throws Exception {
        System.out.println("\n=== ChatGLM Connection Validation Test ===");
        try {
            client.validateConnection();
            System.out.println("Connection validation successful");
        } catch (Exception e) {
            System.out.println("Connection validation failed: " + e.getMessage());
            throw e;
        }
        System.out.println("=====================================\n");
    }
}