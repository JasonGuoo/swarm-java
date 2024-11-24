package org.icespace.swarm.examples.triage;

import org.icespace.swarm.core.Swarm;
import org.icespace.swarm.examples.util.ExampleEnvironment;
import org.icespace.swarm.llm.LLMClient;
import org.icespace.swarm.llm.model.Message;
import org.icespace.swarm.core.SwarmResponse;

import java.util.Arrays;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * Example demonstrating the Triage Agent system
 * 
 * This example shows:
 * 1. Multi-agent interaction
 * 2. Dynamic agent switching
 * 3. Context preservation across agents
 */
@Slf4j
public class TriageExample {

    public static void main(String[] args) {
        // Initialize the framework
        LLMClient client = ExampleEnvironment.createLLMClient();
        Swarm swarm = new Swarm(client);

        // Create initial triage agent
        TriageAgent agent = new TriageAgent();

        // Example 1: Sales inquiry
        Message salesMessage = Message.builder()
                .role("user")
                .content("I'm interested in buying some bees for my garden")
                .build();

        log.info("User: {}", salesMessage.getContent());
        SwarmResponse response = swarm.run(
                agent,
                Arrays.asList(salesMessage),
                new HashMap<>(),
                null,
                false,
                true,
                10);
        log.info(" ===== Assistant: {}", response.getLastMessage().getContent());

        // Example 2: Refund request
        Message refundMessage = Message.builder()
                .role("user")
                .content("I need a refund for item_123, it was too expensive")
                .build();

        log.info("\nUser: {}", refundMessage.getContent());
        response = swarm.run(
                agent,
                Arrays.asList(refundMessage),
                new HashMap<>(),
                null,
                false,
                true,
                10);
        log.info("======= Assistant: {}", response.getLastMessage().getContent());

        // Example 3: Mixed inquiry
        Message mixedMessage = Message.builder()
                .role("user")
                .content("I bought some bees but they're too expensive. What other options do I have?")
                .build();

        log.info("\nUser: {}", mixedMessage.getContent());
        response = swarm.run(
                agent,
                Arrays.asList(mixedMessage),
                new HashMap<>(),
                null,
                false,
                true,
                10);
        log.info("======== Assistant: {}", response.getLastMessage().getContent());
    }
}
