package org.icespace.swarm.examples.weather;

import org.icespace.swarm.core.Swarm;
import org.icespace.swarm.llm.LLMClient;
import org.icespace.swarm.llm.model.ChatResponse;
import org.icespace.swarm.llm.model.Message;
import org.icespace.swarm.examples.util.ExampleEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WeatherExample {
    private static final Logger log = LoggerFactory.getLogger(WeatherExample.class);

    public static void main(String[] args) {
        // Create LLM client based on available configuration
        LLMClient client = ExampleEnvironment.createLLMClient();

        // Create the Swarm instance
        Swarm swarm = new Swarm(client);

        // Create the weather agent
        WeatherAgent agent = new WeatherAgent(client);

        // Initialize context with preferences
        Map<String, Object> context = new HashMap<>();
        context.put("temperature_unit", "fahrenheit");
        context.put("email_signature", "\n\nBest regards,\nWeather Bot");

        try {
            // Test Scenario 1: Basic weather query
            log.info("Test Scenario 1: Basic weather query");
            Message weatherQuery = Message.builder()
                    .role("user")
                    .content("What's the weather like in San Francisco?")
                    .build();
            
            ChatResponse response1 = swarm.run(
                    agent,
                    Arrays.asList(weatherQuery),
                    context,
                    null,
                    false,
                    true,
                    10);
            log.info("Response: {}", response1.getChoices().get(0).getMessage().getContent());

            // Test Scenario 2: Weather + Email
            log.info("\nTest Scenario 2: Weather + Email combination");
            Message emailQuery = Message.builder()
                    .role("user")
                    .content("What's the weather like in San Francisco? Send an email about it to test@example.com")
                    .build();
            
            ChatResponse response2 = swarm.run(
                    agent,
                    Arrays.asList(emailQuery),
                    context,
                    null,
                    false,
                    true,
                    10);
            log.info("Response: {}", response2.getChoices().get(0).getMessage().getContent());

            // Test Scenario 3: Streaming example
            log.info("\nTest Scenario 3: Streaming weather updates");
            Message streamQuery = Message.builder()
                    .role("user")
                    .content("Give me detailed weather information for New York City")
                    .build();
            
            swarm.run(
                    agent,
                    Arrays.asList(streamQuery),
                    context,
                    null,
                    true,  // Enable streaming
                    true,
                    10);

            // Test Scenario 4: Error handling
            log.info("\nTest Scenario 4: Error handling");
            Message errorQuery = Message.builder()
                    .role("user")
                    .content("Send weather update to invalid-email")
                    .build();
            
            try {
                swarm.run(
                        agent,
                        Arrays.asList(errorQuery),
                        context,
                        null,
                        false,
                        true,
                        10);
            } catch (Exception e) {
                log.error("Expected error occurred: {}", e.getMessage());
            }

            // Print final context state
            log.info("\nFinal context state:");
            context.forEach((key, value) -> log.info("{}: {}", key, value));

        } catch (Exception e) {
            log.error("Error running weather example: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}