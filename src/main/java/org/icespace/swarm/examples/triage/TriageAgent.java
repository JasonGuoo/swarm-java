package org.icespace.swarm.examples.triage;

import org.icespace.swarm.core.Agent;
import org.icespace.swarm.core.ToolChoice;
import org.icespace.swarm.function.annotations.FunctionSpec;
import org.icespace.swarm.function.annotations.Parameter;

import java.util.Map;

/**
 * TriageAgent - Routes user requests to appropriate specialized agents
 * 
 * This agent demonstrates:
 * 1. Agent-to-agent communication
 * 2. Dynamic routing based on user intent
 * 3. Function-based agent switching
 */
public class TriageAgent extends Agent {

    /**
     * Constructor - Initialize the triage agent
     */
    public TriageAgent() {
        super();
    }

    @Override
    public String getSystemPrompt(Map<String, Object> context) {
        return "You are a customer service triage agent.\n" +
                "Your role is to:\n" +
                "1. Understand the user's request\n" +
                "2. Route them to the appropriate specialized agent\n\n" +
                "Available agents:\n" +
                "- Sales Agent (transferToSales): For product inquiries and purchases\n" +
                "- Refunds Agent (transferToRefunds): For refunds and discounts\n\n" +
                "Guidelines:\n" +
                "1. Always transfer to the most appropriate agent\n" +
                "2. If unsure, ask clarifying questions\n" +
                "3. Be polite and professional\n";
    }

    @FunctionSpec(description = "Transfer the conversation to the sales agent for product inquiries")
    public Agent transferToSales(Map<String, Object> context) {
        return new SalesAgent();
    }

    @FunctionSpec(description = "Transfer the conversation to the refunds agent for refund and discount requests")
    public Agent transferToRefunds(Map<String, Object> context) {
        return new RefundsAgent();
    }
}
