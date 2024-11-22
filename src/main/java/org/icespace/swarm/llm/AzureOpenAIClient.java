package org.icespace.swarm.llm;

import org.icespace.swarm.llm.model.ChatRequest;
import org.icespace.swarm.llm.model.ChatResponse;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Azure OpenAI Service client implementation.
 * Provides access to OpenAI models hosted on Azure:
 * - GPT-3.5, GPT-4 deployments
 * - Azure-specific authentication
 * - Custom deployment names
 * - Regional endpoints
 *
 * Features:
 * - Azure OpenAI API compatibility
 * - Deployment-based model selection
 * - Azure Active Directory authentication
 * - Regional endpoint support
 * - Error handling with Azure-specific details
 *
 * Configuration:
 * - API key (from Azure portal)
 * - Deployment name (your model deployment)
 * - Base URL (your Azure endpoint)
 *
 * Example usage:
 * <pre>{@code
 * AzureOpenAIClient client = new AzureOpenAIClient(
 *     "your-api-key",
 *     "your-deployment",
 *     "https://your-resource.openai.azure.com"
 * );
 *
 * ChatResponse response = client.chat(request);
 * }</pre>
 *
 * Note: Requires an Azure subscription and OpenAI resource
 */
public class AzureOpenAIClient extends OpenAIClient {
    private final String deploymentId;
    private final String apiVersion;

    public AzureOpenAIClient(String endpoint, String apiKey, String deploymentId, String apiVersion, String model) {
        super(endpoint, apiKey, model);
        this.deploymentId = deploymentId;
        this.apiVersion = apiVersion;
    }

    protected String getEndpointUrl() {
        return String.format("%s/openai/deployments/%s/chat/completions?api-version=%s",
                baseUrl, deploymentId, apiVersion);
    }

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