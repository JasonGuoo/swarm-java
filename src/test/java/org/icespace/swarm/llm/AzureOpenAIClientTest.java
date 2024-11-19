package org.icespace.swarm.llm;

import org.icespace.swarm.llm.model.ChatRequest;
import org.icespace.swarm.llm.model.ChatResponse;
import org.icespace.swarm.llm.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AzureOpenAIClientTest {
    private AzureOpenAIClient client;

    @BeforeEach
    void setUp() {
        String endpoint = TestEnv.require("AZURE_OPENAI_ENDPOINT");
        String apiKey = TestEnv.require("AZURE_OPENAI_KEY");
        String deployment = TestEnv.require("AZURE_OPENAI_DEPLOYMENT");
        String apiVersion = TestEnv.get("AZURE_OPENAI_API_VERSION");
        String model = TestEnv.require("AZURE_OPENAI_MODEL");

        if (apiVersion == null) {
            apiVersion = "2023-05-15"; // default version
        }

        client = new AzureOpenAIClient(endpoint, apiKey, deployment, apiVersion, model);
    }

    @Test
    void testConstructor() {
        assertNotNull(client);
    }

    @Test
    void testEndpointUrl() {
        String expectedUrl = client.getEndpointUrl();
        assertNotNull(expectedUrl);
    }

    @Test
    void testHeaders() {
        var headers = client.getHeaders();
        assertNotNull(headers);
        assertEquals(client.getApiKey(), headers.get("api-key"));
        assertEquals("application/json", headers.get("Content-Type"));
    }

    @Test
    void testBasicChatRequest() throws LLMException {
        ChatRequest request = ChatRequest.builder()
                .model("gpt-35-turbo") // Azure model name
                .messages(List.of(
                        Message.builder()
                                .role("user")
                                .content("Hello!")
                                .build()))
                .build();

        ChatResponse response = client.chat(request);
        assertNotNull(response);
        assertNotNull(response.getChoices());
        assertFalse(response.getChoices().isEmpty());
    }

    @Test
    void testInvalidApiKey() {
        AzureOpenAIClient invalidClient = new AzureOpenAIClient(
                client.getEndpointUrl(), "invalid-key", client.getDeploymentId(), client.getApiVersion(),
                client.getModel());

        ChatRequest request = ChatRequest.builder()
                .model("gpt-35-turbo")
                .messages(List.of(
                        Message.builder()
                                .role("user")
                                .content("Hello!")
                                .build()))
                .build();

        assertThrows(LLMException.class, () -> invalidClient.chat(request));
    }

    @Test
    void testInvalidDeployment() {
        AzureOpenAIClient invalidClient = new AzureOpenAIClient(
                client.getEndpointUrl(), client.getApiKey(), "invalid-deployment", client.getApiVersion(),
                client.getModel());

        ChatRequest request = ChatRequest.builder()
                .model("gpt-35-turbo")
                .messages(List.of(
                        Message.builder()
                                .role("user")
                                .content("Hello!")
                                .build()))
                .build();

        assertThrows(LLMException.class, () -> invalidClient.chat(request));
    }
}