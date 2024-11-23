# Weather Agent Example

This example demonstrates how to create a weather assistant using the Swarm Java framework. The agent can check weather conditions and send email notifications, showcasing key features of the framework.

## Features Demonstrated
- Custom agent implementation with tool functions
- Function annotations with `@FunctionSpec` and `@Parameter`
- System prompt design and tool choice configuration
- Input validation and error handling
- JSON response formatting
- Streaming and non-streaming responses
- Context management for state persistence

## Implementation Guide

### 1. Create Your Agent Class

```java
public class WeatherAgent extends Agent {
    public WeatherAgent() {
        super();
    }
}
```

### 2. Define System Prompt

The system prompt is crucial for guiding the LLM's behavior:

```java
@Override
public String getSystemPrompt(Map<String, Object> context) {
    return "You are a helpful weather assistant that can:\n" +
           "1. Get current weather information\n" +
           "2. Send weather updates via email\n" +
           "Always provide clear and concise responses.";
}
```

### 3. Configure Tool Choice

```java
@Override
public ToolChoice getToolChoice() {
    return ToolChoice.AUTO;  // Let LLM choose appropriate tools
}
```

### 4. Implement Tool Functions

Add functions with proper annotations:

```java
@FunctionSpec(description = "Get the current weather in a given location")
public String getWeather(
    @Parameter(description = "The city and state") String location,
    Map<String, Object> context) {
    // Implementation
}

@FunctionSpec(description = "Send weather update email")
public Object sendEmail(
    @Parameter(description = "Email recipient") String to,
    @Parameter(description = "Email subject", defaultValue = "Weather Update") String subject,
    @Parameter(description = "Email body") String body,
    Map<String, Object> context) {
    // Implementation
}
```

### 5. Using the Agent

Basic usage pattern:

```java
// Initialize the framework
LLMClient client = ExampleEnvironment.createClient();
Swarm swarm = new Swarm(client);
WeatherAgent agent = new WeatherAgent();

// Create a message
Message message = Message.builder()
    .role("user")
    .content("What's the weather in San Francisco?")
    .build();

// Run the agent
SwarmResponse response = swarm.run(
    agent,                    // Your agent
    Arrays.asList(message),   // Messages
    new HashMap<>(),         // Context
    null,                    // Model override (optional)
    false,                   // Streaming mode
    true,                    // Debug mode
    10                       // Max turns
);

// Get the response
System.out.println(response.getLastMessage());
```

## Advanced Features

### 1. Streaming Responses

Enable streaming for real-time responses:

```java
SwarmResponse response = swarm.run(
    agent,
    messages,
    context,
    null,
    true,  // Enable streaming
    true,
    10
);
```

### 2. Context Management

Use context to store session state:

```java
Map<String, Object> context = new HashMap<>();
context.put("temperature_unit", "fahrenheit");
context.put("email_signature", "Best regards,\nWeather Bot");
```

### 3. Error Handling

Implement proper validation and error handling:

```java
if (location == null || location.trim().isEmpty()) {
    throw new IllegalArgumentException("Location cannot be empty");
}
```

## Best Practices

1. **System Prompts**
   - Be specific about agent capabilities
   - Include clear instructions
   - Define response format expectations

2. **Tool Functions**
   - Use descriptive annotations
   - Implement proper validation
   - Return structured responses
   - Handle errors gracefully

3. **Response Formatting**
   - Use consistent JSON structure
   - Include all relevant information
   - Handle special characters properly

4. **Context Management**
   - Use context for session state
   - Implement proper cleanup
   - Consider thread safety

## Example Scenarios

The example includes four test scenarios:
1. Basic weather query
2. Weather + Email combination
3. Streaming weather updates
4. Error handling demonstration

Each scenario demonstrates different aspects of the framework's capabilities.

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
