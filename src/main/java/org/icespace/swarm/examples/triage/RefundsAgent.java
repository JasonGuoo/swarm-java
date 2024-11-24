package org.icespace.swarm.examples.triage;

import org.icespace.swarm.core.Agent;
import org.icespace.swarm.core.ToolChoice;
import org.icespace.swarm.function.annotations.FunctionSpec;
import org.icespace.swarm.function.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * RefundsAgent - Handles refunds and discounts
 * 
 * This agent demonstrates:
 * 1. Multiple tool functions
 * 2. Parameter validation
 * 3. Return to triage capability
 */
@Slf4j
public class RefundsAgent extends Agent {

    public RefundsAgent() {
        super();
    }

    @Override
    public String getSystemPrompt(Map<String, Object> context) {
        return "You are a helpful refunds agent.\n" +
                "Your role is to:\n" +
                "1. Process refund requests\n" +
                "2. Apply discounts when appropriate\n" +
                "3. Handle customer concerns about pricing\n\n" +
                "Guidelines:\n" +
                "1. For expensive items, offer a discount first\n" +
                "2. Process refunds only if the customer insists\n" +
                "3. Always get item_id for refunds (format: item_...)\n" +
                "4. Use transfer_back_to_triage() for non-refund queries\n";
    }

    @Override
    public ToolChoice getToolChoice() {
        return ToolChoice.AUTO;
    }

    @FunctionSpec(description = "Process a refund for a specific item")
    public String processRefund(
            @Parameter(description = "The ID of the item to refund (must start with 'item_')") String itemId,
            @Parameter(description = "Reason for the refund", defaultValue = "NOT SPECIFIED") String reason,
            Map<String, Object> context) {
        
        // Validate item ID format
        if (itemId == null || !itemId.startsWith("item_")) {
            throw new IllegalArgumentException("Invalid item ID format. Must start with 'item_'");
        }

        // Mock refund processing
        log.info("[MOCK] Processing refund for {} (Reason: {})", itemId, reason);
        return "Refund processed successfully for " + itemId;
    }

    @FunctionSpec(description = "Apply a discount to the user's cart")
    public String applyDiscount(Map<String, Object> context) {
        // Mock discount application
        log.info("[MOCK] Applying standard discount");
        return "Applied 11% discount to your cart";
    }

    @FunctionSpec(description = "Transfer back to triage agent if the query is not refund-related")
    public Agent transferBackToTriage(Map<String, Object> context) {
        return new TriageAgent();
    }
}
