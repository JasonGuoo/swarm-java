/**
 * WeatherAgent - Example implementation of a Swarm Agent
 * 
 * This class demonstrates how to create a custom agent using the Swarm framework.
 * Key components of an agent implementation:
 * 1. Extend the Agent class
 * 2. Implement required methods (getSystemPrompt, getToolChoice)
 * 3. Define tool functions using @FunctionSpec annotation
 */
package org.icespace.swarm.examples.weather;

import org.icespace.swarm.core.Agent;
import org.icespace.swarm.core.ToolChoice;
import org.icespace.swarm.function.annotations.FunctionSpec;
import org.icespace.swarm.function.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.regex.Pattern;

public class WeatherAgent extends Agent {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Constructor - Initialize the agent with an LLM client
     * The LLM client is required for communication with the language model
     */
    public WeatherAgent() {
        super();
    }

    /**
     * System Prompt - Defines the agent's behavior and capabilities
     * This is the first message sent to the LLM to establish the context
     * 
     * Best practices for system prompts:
     * 1. Clearly define the agent's role and capabilities
     * 2. Specify the expected input and output formats
     * 3. Include any special instructions or constraints
     */
    @Override
    public String getSystemPrompt(Map<String, Object> context) {
        return "You are a helpful weather assistant that can check the weather and send emails.\n" +
                "You can:\n" +
                "1. Get the current weather for a location using get_weather()\n" +
                "2. Send emails about the weather using send_email()\n\n" +
                "When asked about the weather, always use get_weather() to get accurate information.\n" +
                "When asked to send an email about the weather, first get the weather then use send_email().\n" +
                "If there's an error getting the weather, explain the issue to the user.\n\n" +
                "Be concise and friendly in your responses.";
    }

    /**
     * Tool Choice - Defines how the agent selects tools
     * AUTO: Let the LLM choose appropriate tools
     * NONE: No tools available
     * FUNCTION: Specific function(s) to be used
     */
    @Override
    public ToolChoice getToolChoice() {
        return ToolChoice.AUTO;
    }

    /**
     * Weather Tool Function
     * 
     * @FunctionSpec annotation defines:
     * - Function name and description for the LLM
     * - Parameter descriptions and constraints
     * 
     * @Parameter annotation for each parameter:
     * - Description: Clear explanation of the parameter
     * - Default values (if applicable)
     * - Required/Optional status
     */
    @FunctionSpec(description = "Get the current weather in a given location")
    public String getWeather(
            @Parameter(description = "The city and state, e.g. San Francisco, CA") String location,
            Map<String, Object> context) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be empty");
        }

        // Rate limiting
        String rateLimitKey = "weather_last_call";
        Long lastCall = (Long) context.get(rateLimitKey);
        if (lastCall != null && System.currentTimeMillis() - lastCall < 1000) {
            throw new IllegalStateException("Rate limit exceeded. Please wait before making another request.");
        }
        context.put(rateLimitKey, System.currentTimeMillis());

        try {
            // In a real implementation, you would call a weather API here
            WeatherResponse weather = new WeatherResponse(
                location, 
                "72°F", 
                "Sunny", 
                "45%",
                "10 mph NW",
                "Clear skies for the next 24 hours"
            );
            
            return weather.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get weather for location: " + location, e);
        }
    }

    /**
     * Email Tool Function
     * 
     * Example of a function with multiple parameters and input validation
     * Shows how to:
     * 1. Use default values in @Parameter
     * 2. Validate input parameters
     * 3. Return results with context updates
     */
    @FunctionSpec(description = "Send an email with the weather information")
    public Object sendEmail(
            @Parameter(description = "Email recipient") String to,
            @Parameter(description = "Email subject", defaultValue = "Weather Update") String subject,
            @Parameter(description = "Email body") String body,
            Map<String, Object> context) {
        // Validate email format
        if (!to.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email address format");
        }

        // Validate content
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Email body cannot be empty");
        }

        // Add signature if available in context
        String signature = (String) context.get("email_signature");
        if (signature != null) {
            body += signature;
        }

        // In a real implementation, you would send an actual email here
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);

        return "Email sent successfully";
    }

    /**
     * Helper class for weather data
     * 
     * Best practices:
     * 1. Use clear field names
     * 2. Include JsonProperty annotations for serialization
     * 3. Implement toString() for readable output
     */
    public static class WeatherResponse {
        @JsonProperty private final String location;
        @JsonProperty private final String temperature;
        @JsonProperty private final String conditions;
        @JsonProperty private final String humidity;
        @JsonProperty private final String wind;
        @JsonProperty private final String forecast;

        public WeatherResponse(String location, String temperature, String conditions, 
                             String humidity, String wind, String forecast) {
            this.location = location;
            this.temperature = temperature;
            this.conditions = conditions;
            this.humidity = humidity;
            this.wind = wind;
            this.forecast = forecast;
        }

        public String toString() {
            return String.format(
                "{" +
                "\"location\":\"%s\"," +
                "\"temperature\":\"%s\"," +
                "\"conditions\":\"%s\"," +
                "\"humidity\":\"%s\"," +
                "\"wind\":\"%s\"," +
                "\"forecast\":\"%s\"" +
                "}",
                escapeJson(location),
                escapeJson(temperature),
                escapeJson(conditions),
                escapeJson(humidity),
                escapeJson(wind),
                escapeJson(forecast)
            );
        }

        private String escapeJson(String str) {
            if (str == null) return "";
            return str.replace("\"", "\\\"")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r")
                     .replace("\t", "\\t");
        }
    }
}