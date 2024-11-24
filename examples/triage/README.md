# Swarm Java Triage Example

This example demonstrates how to implement a multi-agent system using the Swarm Java framework. The triage system consists of three specialized agents that work together to handle customer service requests.

## System Overview

The triage system implements three agents:

1. **TriageAgent**: Routes incoming requests to specialized agents
2. **SalesAgent**: Handles product inquiries and purchases
3. **RefundsAgent**: Processes refunds and applies discounts

## Implementation Guide

### 1. Creating Agents

Each agent extends the base `Agent` class and implements its specific functionality:

```java
public class TriageAgent extends Agent {
    @Override
    public String getSystemPrompt(Map<String, Object> context) {
        return "Clear role description and available functions...";
    }

    @FunctionSpec(description = "Transfer to sales agent")
    public Agent transferToSales(Map<String, Object> context) {
        return new SalesAgent();
    }
}
```

Key components:
- `@Slf4j`: Use proper logging
- `getSystemPrompt()`: Define agent's role and capabilities
- `@FunctionSpec`: Annotate available functions

### 2. System Prompts

System prompts are crucial for agent behavior. Follow these guidelines:

```java
@Override
public String getSystemPrompt(Map<String, Object> context) {
    return "You are a customer service triage agent.\n" +
           "Your role is to: ...";
}
```

Best practices:
- Clearly define the agent's role
- List available functions with exact names
- Provide decision-making guidelines
- Include any constraints or policies

### 3. Function Implementation

Implement functions with proper annotations and validation:

```java
@FunctionSpec(description = "Process a refund for a specific item")
public String processRefund(
        @Parameter(description = "Item ID (must start with 'item_')") String itemId,
        @Parameter(description = "Reason for refund") String reason,
        Map<String, Object> context) {
    
    if (itemId == null || !itemId.startsWith("item_")) {
        throw new IllegalArgumentException("Invalid item ID format");
    }
    
    log.info("[MOCK] Processing refund for {} (Reason: {})", itemId, reason);
    return "Refund processed successfully for " + itemId;
}
```

Key points:
- Use descriptive `@FunctionSpec` annotations
- Document parameters with `@Parameter`
- Validate inputs
- Use proper logging
- Return meaningful responses

### 4. Agent Communication

Implement transfer functions to switch between agents:

```java
@FunctionSpec(description = "Transfer to refunds agent")
public Agent transferToRefunds(Map<String, Object> context) {
    return new RefundsAgent();
}
```

## Running the Example

1. Initialize the framework:
```java
LLMClient client = ExampleEnvironment.createLLMClient();
Swarm swarm = new Swarm(client);
```

2. Create the initial agent:
```java
TriageAgent agent = new TriageAgent();
```

3. Create a user message:
```java
Message message = Message.builder()
        .role("user")
        .content("I need a refund for item_123")
        .build();
```

4. Run the agent:
```java
SwarmResponse response = swarm.run(
        agent,
        Arrays.asList(message),
        new HashMap<>(),
        null,
        false,
        true,
        10);
```

## Example Interactions

The example demonstrates three scenarios:

1. Sales inquiry:
```
User: I'm interested in buying some bees for my garden
Assistant: Let me transfer you to our sales agent...
```

2. Refund request:
```
User: I need a refund for item_123
Assistant: I'll transfer you to our refunds agent...
```

3. Mixed inquiry:
```
User: I bought bees but they're too expensive
Assistant: I understand you have concerns about pricing...
```

## Implementation Tips

1. **System Prompts**
   - Be specific about agent roles
   - List functions with exact names
   - Include decision guidelines

2. **Function Design**
   - Use clear, descriptive names
   - Validate all inputs
   - Return meaningful responses

3. **Logging**
   - Use SLF4J with @Slf4j
   - Log important operations
   - Include relevant context

4. **Error Handling**
   - Validate inputs
   - Use proper exceptions
   - Provide clear error messages

## Common Patterns

1. **Agent Transfer**
   - Clear transfer functions
   - Proper context handling
   - Clean state management

2. **Input Validation**
   - Check required parameters
   - Validate format (e.g., item_id)
   - Throw descriptive exceptions

3. **Response Handling**
   - Clear, actionable responses
   - Proper error messages
   - Consistent formatting
