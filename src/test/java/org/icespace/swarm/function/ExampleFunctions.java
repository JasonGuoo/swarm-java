package org.icespace.swarm.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.icespace.swarm.function.annotations.FunctionSpec;
import org.icespace.swarm.function.annotations.Parameter;

public class ExampleFunctions {

    @FunctionSpec(description = "Perform mathematical calculation")
    public double calculate(
            @Parameter(description = "Mathematical expression to evaluate") String expression) {
        // Simplified example: just parse and evaluate basic expressions
        String[] parts = expression.split("[+\\-*/]");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expression must be in format: number operator number");
        }
        
        double a = Double.parseDouble(parts[0].trim());
        double b = Double.parseDouble(parts[1].trim());
        char operator = expression.replaceAll("[^+\\-*/]", "").charAt(0);
        
        switch (operator) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': return a / b;
            default: throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    @FunctionSpec(description = "Search knowledge base")
    public List<String> search(
            @Parameter(description = "Search query") String query,
            @Parameter(description = "Maximum number of results") int limit) {
        // Example implementation
        List<String> results = new ArrayList<>();
        results.add("Result 1 for: " + query);
        results.add("Result 2 for: " + query);
        results.add("Result 3 for: " + query);
        return results.subList(0, Math.min(limit, results.size()));
    }
}