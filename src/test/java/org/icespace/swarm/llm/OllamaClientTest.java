package org.icespace.swarm.llm;

import org.icespace.swarm.llm.model.ChatRequest;
import org.icespace.swarm.llm.model.ChatResponse;
import org.icespace.swarm.llm.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Disabled;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Skipping Ollama tests - requires external service")
class OllamaClientTest {
    private OllamaClient client;

    @BeforeEach
    void setUp() {
        String baseUrl = TestEnv.get("OLLAMA_BASE_URL");
        String model = TestEnv.require("OLLAMA_MODEL");

        client = baseUrl != null ? new OllamaClient(baseUrl, model) : new OllamaClient(model);
    }

    @Test
    void testConstructorWithDefaultEndpoint() {
        String model = TestEnv.require("OLLAMA_MODEL");
        OllamaClient defaultClient = new OllamaClient(model);
        assertNotNull(defaultClient);
    }

    @Test
    void testConstructorWithCustomEndpoint() {
        String model = TestEnv.require("OLLAMA_MODEL");
        String customEndpoint = "http://custom-endpoint:11434";
        OllamaClient customClient = new OllamaClient(customEndpoint, model);
        assertNotNull(customClient);
    }

    @Test
    void testBasicChatRequest() throws LLMException {
        ChatRequest request = ChatRequest.builder()
                .model(client.getModel())
                .messages(List.of(
                        Message.builder()
                                .role("user")
                                .content("Hello! What is your name?")
                                .build()))
                .build();

        ChatResponse response = client.chat(request);
        assertNotNull(response);
        assertNotNull(response.getChoices());
        assertFalse(response.getChoices().isEmpty());
    }

    @Test
    void testEndpointUrl() {
        String baseUrl = TestEnv.get("OLLAMA_BASE_URL");
        if (baseUrl == null) {
            baseUrl = "http://localhost:11434/api";
        }
        assertTrue(client.getEndpointUrl().startsWith(baseUrl));
    }

    @Test
    void testHeaders() {
        Map<String, String> headers = client.getHeaders();
        assertEquals("application/json", headers.get("Content-Type"));
    }

    @Test
    void testConnectionValidation() {
        assertDoesNotThrow(() -> client.validateConnection());
    }

    @Test
    void testInvalidEndpoint() {
        String model = TestEnv.require("OLLAMA_MODEL");
        OllamaClient invalidClient = new OllamaClient("http://invalid-endpoint:11434", model);

        ChatRequest request = ChatRequest.builder()
                .model(model)
                .messages(List.of(
                        Message.builder()
                                .role("user")
                                .content("Hello!")
                                .build()))
                .build();

        assertThrows(LLMException.class, () -> invalidClient.chat(request));
    }

    @Test
    void testInvalidModel() {
        OllamaClient invalidClient = new OllamaClient("invalid-model");

        ChatRequest request = ChatRequest.builder()
                .model("invalid-model")
                .messages(List.of(
                        Message.builder()
                                .role("user")
                                .content("Hello!")
                                .build()))
                .build();

        assertThrows(LLMException.class, () -> invalidClient.chat(request));
    }
}