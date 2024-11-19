package org.icespace.swarm.llm;

import java.util.HashMap;
import java.util.Map;

/**
 * Azure OpenAI API client implementation.
 * Extends OpenAI client and overrides authentication and endpoint handling.
 */
public class AzureOpenAIClient extends OpenAIClient {
    private final String deploymentId;
    private final String apiVersion;

    public AzureOpenAIClient(String endpoint, String apiKey, String deploymentId, String apiVersion, String model) {
        super(endpoint, apiKey, model);
        this.deploymentId = deploymentId;
        this.apiVersion = apiVersion;
    }

    @Override
    protected String getEndpointUrl() {
        return String.format("%s/openai/deployments/%s/chat/completions?api-version=%s",
                baseUrl, deploymentId, apiVersion);
    }

    @Override
    protected Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("api-key", apiKey);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    // Getters for test access
    public String getDeploymentId() {
        return deploymentId;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}