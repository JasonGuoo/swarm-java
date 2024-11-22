package org.icespace.swarm.function;

import org.icespace.swarm.function.annotations.FunctionSpec;
import org.icespace.swarm.function.annotations.Parameter;

import java.util.*;
import java.util.stream.Collectors;

public class TestFunctions {

    @FunctionSpec(description = "Add two numbers")
    public Double add(
            @Parameter(description = "First number") Double a,
            @Parameter(description = "Second number") Double b) {
        return a + b;
    }

    @FunctionSpec(description = "Multiply two numbers")
    public Double multiply(
            @Parameter(description = "First number") Double a,
            @Parameter(description = "Second number") Double b) {
        return a * b;
    }

    @FunctionSpec(description = "Greet someone")
    public String greet(
            @Parameter(description = "Name to greet") String name,
            @Parameter(description = "Use formal greeting") Boolean formal) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        return (formal != null && formal) ? "Dear " + name : "Hi " + name;
    }

    @FunctionSpec(description = "Process a list of items with a prefix")
    public List<String> listItems(
            @Parameter(description = "Items to process") List<String> items,
            @Parameter(description = "Prefix to add") String prefix) {
        return items.stream()
                .map(item -> prefix + (item != null ? item : "null"))
                .collect(Collectors.toList());
    }

    @FunctionSpec(description = "Process data map")
    public Map<String, Object> processData(
            @Parameter(description = "Data to process") Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>(data);
        result.put("processed", true);
        return result;
    }

    @FunctionSpec(description = "Concatenate strings with separator")
    public String concatenate(
            @Parameter(description = "Strings to concatenate") List<String> strings,
            @Parameter(description = "Separator") String separator) {
        return String.join(separator, strings);
    }

    @FunctionSpec(description = "Format text with variables")
    public String formatText(
            @Parameter(description = "Text template") String template,
            @Parameter(description = "Variables for replacement") Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    @FunctionSpec(description = "Calculate basic statistics for numbers")
    public Map<String, Double> calculateStats(
            @Parameter(description = "List of numbers") List<Double> numbers) {
        Map<String, Double> stats = new HashMap<>();
        if (numbers == null || numbers.isEmpty()) {
            return stats;
        }

        stats.put("sum", numbers.stream().mapToDouble(Double::doubleValue).sum());
        stats.put("average", numbers.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
        stats.put("min", numbers.stream().mapToDouble(Double::doubleValue).min().orElse(0.0));
        stats.put("max", numbers.stream().mapToDouble(Double::doubleValue).max().orElse(0.0));
        return stats;
    }

    @FunctionSpec(description = "Process text with various operations")
    public String processText(
            @Parameter(description = "Input text") String text,
            @Parameter(description = "List of operations") List<String> operations) {
        String result = text;
        for (String operation : operations) {
            switch (operation.toLowerCase()) {
                case "uppercase":
                    result = result.toUpperCase();
                    break;
                case "lowercase":
                    result = result.toLowerCase();
                    break;
                case "trim":
                    result = result.trim();
                    break;
                case "reverse":
                    result = new StringBuilder(result).reverse().toString();
                    break;
            }
        }
        return result;
    }
}