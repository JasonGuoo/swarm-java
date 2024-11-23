/**
 * WeatherExample - Demonstrates how to use the Swarm framework with a custom agent
 * 
 * This example shows:
 * 1. How to initialize and configure the Swarm framework
 * 2. Different ways to interact with an agent
 * 3. How to handle responses and errors
 */
package org.icespace.swarm.examples.weather;

import org.icespace.swarm.core.Swarm;
import org.icespace.swarm.core.SwarmResponse;
import org.icespace.swarm.llm.LLMClient;
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
        WeatherAgent agent = new WeatherAgent();

        // Initialize context with preferences
        Map<String, Object> context = new HashMap<>();
        context.put("temperature_unit", "fahrenheit");
        context.put("email_signature", "\n\nBest regards,\nWeather Bot");

        try {
            // Test Scenario 1: Basic weather query
            log.info("Test Scenario 1: Basic weather query");
            // This is a simple query to the weather agent
            Message weatherQuery = Message.builder()
                    .role("user")
                    .content("What's the weather like in San Francisco?")
                    .build();
            
            // Run the query through the Swarm framework
            SwarmResponse response1 = swarm.run(
                    agent,                    // The agent to use
                    Arrays.asList(weatherQuery), // List of messages
                    context,                  // Context map
                    null,                     // Model override (optional)
                    false,                    // Streaming mode
                    true,                     // Debug mode
                    10);                      // Max conversation turns
            log.info("Response of Scenario 1: {}", response1.getLastMessage());

            // Test Scenario 2: Weather + Email
            log.info("\nTest Scenario 2: Weather + Email combination");
            // This query demonstrates how to chain multiple tool calls
            Message emailQuery = Message.builder()
                    .role("user")
                    .content("What's the weather like in San Francisco? Send an email about it to test@example.com")
                    .build();
            
            // Run the query through the Swarm framework
            SwarmResponse response2 = swarm.run(
                    agent,
                    Arrays.asList(emailQuery),
                    context,
                    null,
                    false,
                    true,
                    10);
            log.info("Response of Scenario 2: {}", response2.getLastMessage());

            // Test Scenario 3: Streaming example
            log.info("\nTest Scenario 3: Streaming weather updates");
            // This query shows how to handle streaming responses
            Message streamQuery = Message.builder()
                    .role("user")
                    .content("Give me detailed weather information for New York City")
                    .build();

            // Run the query through the Swarm framework with streaming enabled
            SwarmResponse response3 = swarm.run(
                    agent,
                    Arrays.asList(streamQuery),
                    context,
                    null,
                    true,  // Enable streaming
                    true,
                    10);
            log.info("Response of Scenario 3: {}", response3.getLastMessage());

            // Test Scenario 4: Error handling
            log.info("\nTest Scenario 4: Error handling");
            // This query demonstrates how to handle errors gracefully
            Message errorQuery = Message.builder()
                    .role("user")
                    .content("Send weather update to invalid-email")
                    .build();
            
            try {
                // Run the query through the Swarm framework
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