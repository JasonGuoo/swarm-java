package org.icespace.swarm.llm;

import org.icespace.swarm.llm.model.ChatRequest;
import org.icespace.swarm.llm.model.ChatResponse;
import org.icespace.swarm.llm.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIClientTest {
    private OpenAIClient client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        String apiKey = TestEnv.require("OPENAI_API_KEY");
        String baseUrl = TestEnv.get("OPENAI_BASE_URL");
        String model = TestEnv.require("OPENAI_MODEL");

        client = baseUrl != null ? new OpenAIClient(baseUrl, apiKey, model) : new OpenAIClient(apiKey, model);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testConstructorWithDefaultEndpoint() throws Exception {
        System.out.println("\n=== OpenAI Default Endpoint Test ===");
        OpenAIClient defaultClient = new OpenAIClient(TestEnv.require("OPENAI_API_KEY"), "gpt-3.5-turbo");
        assertNotNull(defaultClient);

        // Test a simple request with default endpoint
        ChatRequest request = ChatRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(Message.builder()
                        .role("user")
                        .content("Hello!")
                        .build()))
                .build();

        System.out.println("Request: " + objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(request));

        try {
            ChatResponse response = defaultClient.chat(request);
            System.out.println("Response: " + objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(response));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println("=====================================\n");
    }

    @Test
    void testBasicChatRequest() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(
                        Message.builder()
                                .role("user")
                                .content("Hello!")
                                .build()))
                .build();

        System.out.println("\n=== OpenAI Basic Chat Test ===");
        System.out.println("Request: " + objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(request));

        ChatResponse response = client.chat(request);

        System.out.println("Response: " + objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(response));
        System.out.println("=====================================\n");

        assertNotNull(response);
        assertNotNull(response.getChoices());
        assertFalse(response.getChoices().isEmpty());
    }

    @Test
    void testWithParameters() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(
                        Message.builder()
                                .role("user")
                                .content("Write a very short poem about AI.")
                                .build()))
                .temperature(0.7)
                .maxTokens(100)
                .build();

        System.out.println("\n=== OpenAI Parameters Test ===");
        System.out.println("Request: " + objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(request));

        ChatResponse response = client.chat(request);

        System.out.println("Response: " + objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(response));
        System.out.println("=====================================\n");

        assertNotNull(response);
    }

    @Test
    void testInvalidApiKey() throws Exception {
        OpenAIClient invalidClient = new OpenAIClient("invalid-key", "gpt-3.5-turbo");
        ChatRequest request = ChatRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(
                        Message.builder()
                                .role("user")
                                .content("Hello!")
                                .build()))
                .build();

        System.out.println("\n=== OpenAI Invalid API Key Test ===");
        System.out.println("Request: " + objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(request));

        try {
            ChatResponse response = invalidClient.chat(request);
            System.out.println("Response: " + objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(response));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println("=====================================\n");

        assertThrows(LLMException.class, () -> invalidClient.chat(request));
    }
}