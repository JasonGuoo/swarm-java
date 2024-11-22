# Weather Agent Example

This example demonstrates how to create a weather assistant using SwarmJava. The agent can check weather conditions and send email notifications.

## Features Demonstrated
- Function annotations with `@FunctionSpec` and `@Parameter`
- Context management for rate limiting and state persistence
- Error handling and input validation
- Integration with external services (weather API and email)
- Custom system prompts
- Automatic tool selection

## Implementation

### 1. Weather Agent Implementation

```java
/**
 * WeatherAgent demonstrates how to create a specialized agent that can check weather
 * and send email notifications. It showcases:
 * - Function annotations and parameter validation
 * - Context management for rate limiting and state persistence
 * - Error handling and input validation
 * - Integration with external services (weather API and email)
 */
public class WeatherAgent extends Agent {
    // Store LLM client for potential direct usage in agent methods
    private final LLMClient client;

    public WeatherAgent(LLMClient client) {
        this.client = client;
    }

    /**
     * Defines the agent's personality and capabilities through system prompt.
     * This is crucial for guiding the LLM's behavior and responses.
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
     * Configures how the agent handles tool/function selection.
     * AUTO allows the LLM to choose which function to call based on user input.
     */
    @Override
    public ToolChoice getToolChoice() {
        return ToolChoice.AUTO;
    }

    /**
     * Gets current weather for a location with built-in rate limiting.
     * Demonstrates:
     * - Parameter validation
     * - Rate limiting using context
     * - Error handling
     * - Context updates for state tracking
     */
    @FunctionSpec(description = "Get the current weather in a given location")
    public Result getWeather(
            @Parameter(description = "The city and state, e.g. San Francisco, CA") String location,
            Map<String, Object> context) {
        // Input validation
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be empty");
        }

        // Implement rate limiting using context
        String rateLimitKey = "weather_last_call";
        Long lastCall = (Long) context.get(rateLimitKey);
        if (lastCall != null && System.currentTimeMillis() - lastCall < 1000) {
            throw new IllegalStateException("Rate limit exceeded. Please wait before making another request.");
        }
        context.put(rateLimitKey, System.currentTimeMillis());

        try {
            // In a real implementation, you would call a weather API here
            // This is just a mock response for demonstration
            String weather = "72°F, Sunny, 45%, 10 mph NW, Clear skies for the next 24 hours";
            
            // Update context with weather information for potential future use
            return new Result(weather)
                    .withContextUpdate("last_location", location)
                    .withContextUpdate("last_weather", weather)
                    .withContextUpdate("last_weather_time", System.currentTimeMillis());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get weather for location: " + location, e);
        }
    }

    /**
     * Sends an email with weather information.
     * Demonstrates:
     * - Input validation with regex
     * - Context usage for customization
     * - Structured response handling
     */
    @FunctionSpec(description = "Send an email with the weather information")
    public Result sendEmail(
            @Parameter(description = "Email recipient") String to,
            @Parameter(description = "Email subject", defaultValue = "Weather Update") String subject,
            @Parameter(description = "Email body") String body,
            Map<String, Object> context) {
        // Validate email format using regex
        if (!to.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email address format");
        }

        // Ensure email content is provided
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Email body cannot be empty");
        }

        // Customize email with signature from context
        String signature = (String) context.get("email_signature");
        if (signature != null) {
            body += signature;
        }

        // In a real implementation, you would integrate with an email service
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);

        // Update context with email tracking information
        return new Result("Email sent successfully")
                .withContextUpdate("last_email_to", to)
                .withContextUpdate("last_email_time", System.currentTimeMillis());
    }
}
```

### 2. Usage Example

```java
/**
 * Example demonstrating how to set up and use the WeatherAgent.
 * Shows:
 * - LLM client initialization
 * - Agent configuration
 * - Context setup
 * - Conversation handling
 */
public class WeatherAgentExample {
    public static void main(String[] args) {
        // Initialize the LLM client with API key from environment
        // You can also use other providers like Azure OpenAI or ChatGLM
        LLMClient client = new OpenAIClient(System.getenv("OPENAI_API_KEY"));

        // Create weather agent instance with the configured client
        WeatherAgent agent = new WeatherAgent(client);

        // Initialize Swarm framework with the client
        Swarm swarm = new Swarm(client);

        // Set up the initial conversation with a user message
        // The agent will process this message and may call multiple functions in response
        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder()
                .role("user")
                .content("What's the weather like in San Francisco? " +
                        "If it's nice, send an email to alice@example.com about it.")
                .build());

        // Initialize context with custom email signature
        // Context can store any state that needs to persist across function calls
        Map<String, Object> context = new HashMap<>();
        context.put("email_signature", "\nBest regards,\nWeather Bot");

        // Run the conversation with streaming enabled (true) and no timeout (null)
        // The agent will automatically:
        // 1. Check the weather using getWeather()
        // 2. Evaluate if it's nice weather
        // 3. Send an email using sendEmail() if appropriate
        ChatResponse response = swarm.run(agent, messages, context, null, false, true);

        // Process and display the conversation results
        for (Message message : response.getMessages()) {
            System.out.println(message.getRole() + ": " + message.getContent());
        }
    }
}
```

## Key Concepts Demonstrated

1. **Function Annotations**
   - `@FunctionSpec` for function descriptions
   - `@Parameter` for parameter documentation
   - Default values for optional parameters

2. **Context Management**
   - Rate limiting implementation
   - State persistence between calls
   - Custom data storage (email signature)

3. **Error Handling**
   - Input validation
   - Exception handling
   - Meaningful error messages

4. **Integration Patterns**
   - External API integration points
   - Email service integration
   - Configuration management

5. **Best Practices**
   - Comprehensive documentation
   - Clean code structure
   - Proper error handling
   - Flexible configuration
