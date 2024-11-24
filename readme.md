# Swarm-Java

A Java implementation of [OpenAI's Swarm framework](https://github.com/openai/swarm) for exploring ergonomic, lightweight multi-agent orchestration.

SwarmJava is a Java library for building LLM-powered applications that enable multiple AI agents to work together seamlessly. The core of Swarm is built around annotations that allow you to create LLM-powered agents and define their interactions through function calling.

## Core Concepts

### 1. Agents and System Prompts
Agents are the fundamental building blocks of Swarm. Each agent has a specific role and expertise, defined by its system prompt and behavior. For example, a Research Agent specializes in analyzing scientific papers, while a Statistics Agent focuses on validating statistical methodologies.

### 2. Function Calling
SwarmJava uses a powerful annotation-based system to expose Java methods as callable functions to the LLM. This system enables seamless communication between the LLM and your code:

1. **Function Discovery**: The framework automatically scans your code for methods annotated with `@FunctionSpec`, which tells the LLM what functions are available and what they do.

2. **Parameter Mapping**: The `@Parameter` annotation on method parameters provides the LLM with:
   - Clear descriptions of what each parameter does
   - Parameter types (automatically inferred from Java types)
   - Default values when applicable

3. **Dynamic Invocation**: When the LLM decides to use a function:
   - It selects the appropriate function based on the descriptions
   - Provides the required parameters in the correct format
   - The framework automatically validates and converts the parameters
   - Your Java method is called with the correct arguments

This annotation-based approach means you don't need to write any boilerplate code for LLM integration - just annotate your methods and the framework handles the rest.

### 3. Dynamic Hand-offs
The key feature of Swarm is its dynamic agent collaboration system, which allows agents to seamlessly work together. Let's look at a customer service system example:

1. **Initial Contact**: A Customer Service Manager Agent receives and analyzes customer requests
2. **Intelligent Routing**: Based on the request type, the manager automatically delegates to specialized agents:
   - Sales Agent: Handles product inquiries, pricing questions, and purchase assistance
   - Refund Agent: Processes return requests and manages refund procedures
   - Manager Agent: Handles escalations and complex situations requiring oversight

For example, when a customer inquiry comes in:
1. The Manager Agent analyzes the request: "I want to return a defective product"
2. Recognizing this as a refund case, it hands off to the Refund Agent
3. If the refund amount exceeds certain limits, the Refund Agent may escalate back to the Manager Agent
4. For product-related questions during the process, the Sales Agent might be consulted

This creates a dynamic, self-organizing system where:
- Agents autonomously decide when to delegate
- Each agent focuses on their area of expertise
- Complex requests are handled efficiently
- Context is maintained throughout the conversation

What makes Swarm powerful:
- **Autonomous Decision Making**: Agents independently decide when to use tools, call functions, or delegate tasks
- **Contextual Awareness**: Built-in context management ensures agents maintain state and understand their environment
- **Flexible Integration**: Simple HTTP-based implementation ensures compatibility across different LLM providers
- **Dynamic Workflows**: LLM-powered decisions for agent transitions create adaptive task handling

## Project Status

The project now has its core functionality ready and stable:

1. **Dynamic Function Calling**: Automatically calls annotated functions based on LLM responses
2. **Agent Switching**: Seamlessly switches between agents based on LLM decisions
3. **LLM Support**: Integrates with multiple LLM providers:
   - OpenAI
   - Azure OpenAI
   - ChatGLM
   - Ollama
4. **Verified Implementation**: Core functionality has been tested and verified with OpenAI API

The framework is now ready for building multi-agent applications while we continue to add more features and improvements.

## Requirements

- Java 11 (LTS) or later
- Maven 3.6+

## Quick Start

For a complete example of how to use SwarmJava, check out our [Weather Agent Example](examples/weather/README.md) which demonstrates:
- Agent definition with system prompts
- Function annotations and parameter validation
- Dynamic agent hand-offs
- Context management and state persistence
- Integration with external services

## Examples

### Weather Agent Example

The Weather Agent demonstrates how to create a simple agent that interacts with external APIs to provide weather information. It shows:
- Basic agent implementation
- External API integration
- Function annotations and parameters

See [Weather Example](examples/weather/README.md) for details.

### Triage Agent Example

The Triage Agent demonstrates how to implement a multi-agent system for customer service routing. It shows:
- Multi-agent coordination
- Dynamic agent switching
- Context preservation across agents
- Function-based routing

The system includes:
- **TriageAgent**: Routes requests to specialized agents
- **SalesAgent**: Handles product inquiries
- **RefundsAgent**: Processes refunds and discounts

See [Triage Example](examples/triage/README.md) for implementation details.

## Configuration

### OpenAI
```java
LLMClient client = new OpenAIClient(apiKey)
    .setModel("gpt-4-0613")
    .setTemperature(0.7);
```

### Azure OpenAI
```java
LLMClient client = new AzureOpenAIClient(apiKey)
    .setDeploymentId("your-deployment")
    .setEndpoint("your-endpoint");
```

### ChatGLM
```java
LLMClient client = new ChatGLMClient()
    .setEndpoint("your-endpoint");
```

### Ollama
```java
LLMClient client = new OllamaClient()
    .setModel("llama2")
    .setEndpoint("http://localhost:11434");
```

## Building from Source

```bash
# Requires Java 11 and Maven 3.6+
git clone https://github.com/JasonGuoo/swarm-java.git
cd swarm-java
mvn clean install
```

## Troubleshooting

Common issues and solutions:
1. API Key errors: Ensure environment variables are set correctly
2. Rate limiting: Implement appropriate delays between requests
3. Model compatibility: Verify model supports function calling
4. Memory usage: Monitor context size for large conversations

## Contributing

We welcome contributions! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
