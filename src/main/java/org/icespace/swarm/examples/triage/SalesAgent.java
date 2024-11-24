package org.icespace.swarm.examples.triage;

import org.icespace.swarm.core.Agent;
import org.icespace.swarm.core.ToolChoice;
import org.icespace.swarm.function.annotations.FunctionSpec;
import org.icespace.swarm.function.annotations.Parameter;

import java.util.Map;

/**
 * SalesAgent - Handles product inquiries and sales
 * 
 * This agent demonstrates:
 * 1. Specialized agent behavior
 * 2. Return to triage functionality
 * 3. Context-aware responses
 */
public class SalesAgent extends Agent {

    public SalesAgent() {
        super();
    }

    @Override
    public String getSystemPrompt(Map<String, Object> context) {
        return "You are an enthusiastic sales agent specializing in bee-related products.\n" +
                "Your role is to:\n" +
                "1. Help customers find the perfect bee products\n" +
                "2. Answer product-related questions\n" +
                "3. Provide pricing information\n\n" +
                "Guidelines:\n" +
                "1. Be enthusiastic about bees and bee products\n" +
                "2. If asked about refunds or non-sales topics, use transfer_back_to_triage()\n" +
                "3. Highlight the benefits and unique features of our products\n" +
                "4. Be helpful and informative\n";
    }


    @FunctionSpec(description = "Transfer back to triage agent if the query is not sales-related")
    public Agent transferBackToTriage(Map<String, Object> context) {
        return new TriageAgent();
    }
}
