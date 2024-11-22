package org.icespace.swarm.examples.util;

import org.icespace.swarm.llm.*;
import org.icespace.swarm.util.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Environment utility for Swarm examples.
 * Handles environment setup and LLM client creation.
 */
public class ExampleEnvironment {
    private static final Logger log = LoggerFactory.getLogger(ExampleEnvironment.class);

    /**
     * Creates an LLM client based on available environment configuration.
     * Checks for various provider configurations in order of preference.
     */
    public static LLMClient createLLMClient() {
        ensureEnvFileExists();

        // Check for OpenAI configuration
        String openaiKey = Environment.get("OPENAI_API_KEY");
        if (openaiKey != null && openaiKey.startsWith("sk-")) {
            log.info("Using OpenAI configuration");
            return new OpenAIClient(Environment.get("OPENAI_BASE_URL"), openaiKey, Environment.get("OPENAI_MODEL"));
        }

        // Check for Azure OpenAI configuration
        String azureKey = Environment.get("AZURE_OPENAI_KEY");
        String azureEndpoint = Environment.get("AZURE_OPENAI_ENDPOINT");
        if (azureKey != null && azureEndpoint != null) {
            log.info("Using Azure OpenAI configuration");
            return new AzureOpenAIClient(
                    azureEndpoint,
                    azureKey,
                    Environment.get("AZURE_OPENAI_DEPLOYMENT_NAME"),
                    Environment.get("AZURE_OPENAI_API_VERSION"),
                    Environment.get("AZURE_OPENAI_MODEL"));
        }

        // Check for ChatGLM configuration
        String chatglmKey = Environment.get("CHATGLM_API_KEY");
        if (chatglmKey != null) {
            log.info("Using ChatGLM configuration");
            return new ChatGLMClient(chatglmKey);
        }

        // Check for Ollama configuration
        String ollamaUrl = Environment.get("OLLAMA_BASE_URL");
        if (ollamaUrl != null) {
            log.info("Using Ollama configuration");
            return new OllamaClient(ollamaUrl);
        }
                
        throw new IllegalStateException(
                "No valid LLM configuration found in environment.\n" +
                "Please create a .env file in one of these locations:\n" +
                "- " + Paths.get("").toAbsolutePath() + "\n" +
                "- " + Paths.get("").toAbsolutePath().getParent() + "\n" +
                "You can find the template at:\n" +
                "- " + Paths.get("src/test/resources/.env.template") + "\n"
        );
    }

    private static void ensureEnvFileExists() {
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path envInRoot = projectRoot.resolve(".env");
        Path envInParent = projectRoot.getParent().resolve(".env");

        if (!Files.exists(envInRoot) && !Files.exists(envInParent)) {
            log.error("No .env file found in the following locations:");
            log.error("- {}", envInRoot);
            log.error("- {}", envInParent);
            log.error("\nTemplate location for reference:");
            throw new IllegalStateException("Please create and configure your .env file before running examples.");
        }
    }
}