package org.icespace.swarm.examples.weather;

import org.icespace.swarm.core.Agent;
import org.icespace.swarm.core.Result;
import org.icespace.swarm.core.ToolChoice;
import org.icespace.swarm.function.annotations.FunctionSpec;
import org.icespace.swarm.function.annotations.Parameter;
import org.icespace.swarm.llm.LLMClient;
import org.icespace.swarm.llm.model.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.stream.Stream;

public class WeatherAgent extends Agent {
    private final LLMClient client;

    public WeatherAgent(LLMClient client) {
        this.client = client;
    }

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

    @Override
    public ToolChoice getToolChoice() {
        return ToolChoice.AUTO;
    }

    @FunctionSpec(description = "Get the current weather in a given location")
    public Result getWeather(
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
            
            return new Result(weather)
                    .withContextUpdate("last_location", location)
                    .withContextUpdate("last_weather", weather)
                    .withContextUpdate("last_weather_time", System.currentTimeMillis());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get weather for location: " + location, e);
        }
    }

    @FunctionSpec(description = "Send an email with the weather information")
    public Result sendEmail(
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

        return new Result("Email sent successfully")
                .withContextUpdate("last_email_to", to)
                .withContextUpdate("last_email_subject", subject)
                .withContextUpdate("last_email_time", System.currentTimeMillis());
    }

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
    }
}