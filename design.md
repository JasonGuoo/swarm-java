# Swarm Java Implementation Design Document

## Table of Contents
- [Swarm Java Implementation Design Document](#swarm-java-implementation-design-document)
  - [Table of Contents](#table-of-contents)
  - [Overview](#overview)
    - [Key Features to Implement](#key-features-to-implement)
    - [Design Goals](#design-goals)
  - [Core Architecture](#core-architecture)
    - [Class Structure](#class-structure)
      - [Core Components](#core-components)
    - [Key Abstractions](#key-abstractions)
    - [Python vs Java Implementation](#python-vs-java-implementation)
  - [Technical Design](#technical-design)
    - [Function Registration System](#function-registration-system)
    - [Context Management](#context-management)
    - [Agent Communication](#agent-communication)
  - [Implementation Challenges](#implementation-challenges)
  - [Development Roadmap](#development-roadmap)
  - [LLM Integration Design](#llm-integration-design)
    - [Core Components](#core-components-1)
    - [Provider Implementations](#provider-implementations)
    - [Error Handling](#error-handling)
    - [Usage Example](#usage-example)
    - [Implementation Considerations](#implementation-considerations)
    - [JSON-Based Communication](#json-based-communication)

## Overview

This document outlines the design for implementing OpenAI's Swarm framework in Java. The goal is to maintain feature parity with the Python implementation while leveraging Java's strengths and ecosystem.

### Key Features to Implement
- Agent-based task orchestration
- Function calling capabilities
- Context variable management
- Agent handoff mechanisms
- Streaming response support

### Design Goals
- Type safety and compile-time checks
- Thread safety and concurrent execution support
- Easy integration with existing Java applications
- Extensible architecture for future enhancements
- Comprehensive testing support

## Core Architecture

### Class Structure

#### Core Components

1. **Swarm Class**
   - Central orchestrator for agent execution
   - Manages OpenAI API communication
   - Handles thread pool for concurrent operations
   - Provides both synchronous and asynchronous APIs

2. **Agent Class**
   - Represents an AI agent with specific capabilities
   - Maintains its own function set and instructions
   - Supports tool choice for function calling strategy
   - Implements builder pattern for flexible construction

3. **Function Registry**
   - Manages function registration and discovery
   - Handles type conversion and parameter validation
   - Caches function metadata for performance
   ```java
   public class FunctionRegistry {
       private final Map<String, Function> functions;
       private final TypeConverter typeConverter;
       
       public void register(Object target, Method method);
       public Function get(String name);
       public List<FunctionSchema> getAllSchemas();
   }
   ```

### Key Abstractions

1. **Message Handling**
   ```java
   public class Message {
       private final String role;
       private final String content;
       private final String sender;
       private final List<ToolCall> toolCalls;
       
       // Immutable message implementation
   }
   
   public class ToolCall {
       private final String id;
       private final String type;
       private final FunctionCall function;
   }
   ```

2. **Context Management**
   ```java
   public class SwarmContext {
       private final ConcurrentMap<String, Object> variables;
       private final ReadWriteLock lock;
       
       public <T> T get(String key, Class<T> type);
       public void set(String key, Object value);
       public Map<String, Object> snapshot();
   }
   ```

### Python vs Java Implementation

1. **Type System Differences**
   
   Python Implementation:
   ```python
   def register_function(func):
       # Dynamic inspection of function
       return func
   ```
   
   Java Implementation:
   ```java
   @SwarmFunction(
       description = "Function description",
       parameters = {
           @Parameter(name = "param1", type = "string", required = true)
       }
   )
   public void javaFunction(String param1) {
       // Implementation
   }
   ```

2. **Streaming Implementation**
   
   Python Implementation:
   ```python
   async def stream_response():
       yield chunk
   ```
   
   Java Implementation:
   ```java
   public interface SwarmStream extends AutoCloseable {
       Optional<ResponseChunk> next();
       CompletableFuture<Response> toFuture();
   }
   ```

## Technical Design

### Function Registration System

1. **Annotation Processing**
   ```java
   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.METHOD)
   public @interface SwarmFunction {
       String description() default "";
       Parameter[] parameters() default {};
       boolean streaming() default false;
   }
   ```

2. **Type Conversion System**
   ```java
   public interface TypeConverter {
       Object convert(String value, Class<?> targetType);
       String toJsonSchema(Class<?> type);
   }
   ```

### Context Management

1. **Thread-Safe Context**
   ```java
   public class ContextManager {
       private final ConcurrentHashMap<String, Object> store;
       
       public <T> T getOrDefault(String key, T defaultValue);
       public void update(String key, UnaryOperator<Object> updater);
   }
   ```

2. **Scope Management**
   ```java
   public class ContextScope implements AutoCloseable {
       private final ContextManager manager;
       private final Map<String, Object> snapshot;
       
       public void commit();
       public void rollback();
   }
   ```

### Agent Communication

1. **Message Queue**
   ```java
   public class MessageQueue {
       private final BlockingQueue<Message> queue;
       
       public void send(Message message);
       public Message receive(Duration timeout);
   }
   ```

2. **Handoff Protocol**
   ```java
   public interface HandoffProtocol {
       boolean canHandle(Agent source, Agent target);
       void transfer(Agent source, Agent target, Context context);
   }
   ```

## Implementation Challenges

1. **Reflection and Type Safety**
   - Challenge: Maintaining type safety with reflection
   - Solution: Type-safe wrappers and compile-time validation

2. **Concurrent Execution**
   - Challenge: Managing shared state
   - Solution: Immutable messages and thread-safe context

3. **Error Handling**
   - Challenge: Consistent error propagation
   - Solution: Structured exception hierarchy

## Development Roadmap

1. **Phase 1: Core Framework **
   - Basic agent implementation
   - Function registration system
   - Context management

2. **Phase 2: Advanced Features **
   - Streaming support
   - Agent handoff mechanism
   - Error recovery system

3. **Phase 3: Integration & Testing **
   - OpenAI client integration
   - Test framework
   - Documentation

## LLM Integration Design

### Core Components

1. **Basic Request/Response Models**
   ```java
   @Getter
   @Builder
   public class ChatRequest {
       private List<Message> messages;
       private Map<String, Object> parameters;  // temperature, max_tokens, etc.
       private List<FunctionSchema> functions;  // optional function definitions
       
       @JsonProperty("messages")
       public List<Message> getMessages() { return messages; }
   }

   @Getter
   @AllArgsConstructor
   public class ChatResponse {
       private String id;
       private String content;
       private List<ToolCall> toolCalls;
       private Map<String, Object> usage;  // token usage info
       
       @JsonCreator
       public ChatResponse(
           @JsonProperty("id") String id,
           @JsonProperty("content") String content,
           @JsonProperty("tool_calls") List<ToolCall> toolCalls,
           @JsonProperty("usage") Map<String, Object> usage
       ) {
           this.id = id;
           this.content = content;
           this.toolCalls = toolCalls;
           this.usage = usage;
       }
   }
   ```

2. **Simple LLM Client Interface**
   ```java
   public interface LLMClient {
       ChatResponse chat(ChatRequest request) throws LLMException;
       Stream<ChatResponse> stream(ChatRequest request) throws LLMException;
   }
   ```

3. **Base Implementation**
   ```java
   public abstract class BaseLLMClient implements LLMClient {
       protected final HttpClient httpClient;
       protected final ObjectMapper objectMapper;
       protected final String apiKey;
       protected final String endpoint;
       
       @Override
       public ChatResponse chat(ChatRequest request) throws LLMException {
           HttpRequest httpRequest = buildRequest(request);
           try {
               HttpResponse<String> response = httpClient.send(httpRequest, 
                   HttpResponse.BodyHandlers.ofString());
               return parseResponse(response);
           } catch (Exception e) {
               throw new LLMException("Failed to complete chat request", e);
           }
       }
       
       protected abstract HttpRequest buildRequest(ChatRequest request);
       protected abstract ChatResponse parseResponse(HttpResponse<String> response) throws LLMException;
   }
   ```

### Provider Implementations

1. **OpenAI Implementation**
   ```java
   public class OpenAIClient extends BaseLLMClient {
       @Override
       protected HttpRequest buildRequest(ChatRequest request) {
           return HttpRequest.newBuilder()
               .uri(URI.create(endpoint + "/chat/completions"))
               .header("Authorization", "Bearer " + apiKey)
               .header("Content-Type", "application/json")
               .POST(HttpRequest.BodyPublishers.ofString(
                   objectMapper.writeValueAsString(request)))
               .build();
       }
   }
   ```

2. **Simple Factory**
   ```java
   public class LLMClientFactory {
       public static LLMClient create(String provider, String apiKey, String endpoint) {
           return switch (provider) {
               case "openai" -> new OpenAIClient(apiKey, endpoint);
               case "azure" -> new AzureOpenAIClient(apiKey, endpoint);
               case "chatglm" -> new ChatGLMClient(apiKey, endpoint);
               case "ollama" -> new OllamaClient(endpoint);
               default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
           };
       }
   }
   ```

### Error Handling

```java
public class LLMException extends RuntimeException {
    private final ErrorType type;
    private final int statusCode;
    
    public enum ErrorType {
        AUTHENTICATION_ERROR,
        RATE_LIMIT_ERROR,
        INVALID_REQUEST,
        SERVER_ERROR,
        NETWORK_ERROR
    }
}
```

### Usage Example

```java
// Example usage
var client = LLMClientFactory.create("openai", "your-api-key", "https://api.openai.com/v1");

var request = ChatRequest.builder()
    .messages(List.of(new Message("user", "Hello!")))
    .parameters(Map.of("temperature", 0.7))
    .build();

try {
    ChatResponse response = client.chat(request);
    System.out.println(response.getContent());
} catch (LLMException e) {
    // Handle error
}
```

### Implementation Considerations

1. **JSON Handling**
   - Using Jackson annotations for direct JSON mapping
   - Consistent naming conventions across providers
   - Proper null handling and optional fields

2. **Error Handling**
   - Provider-specific error codes mapping
   - Consistent exception hierarchy
   - Proper error message propagation

3. **Streaming Support**
   - Using Java 11's HTTP Client for SSE
   - Proper resource cleanup
   - Backpressure handling

4. **Security**
   - API key management
   - Request/response validation
   - Proper error message sanitization

### JSON-Based Communication

1. **JSON Request/Response Structure**
   ```json
   // Example OpenAI Chat Request
   {
       "messages": [
           {
               "role": "user",
               "content": "Hello!"
           }
       ],
       "model": "gpt-3.5-turbo",
       "temperature": 0.7,
       "functions": [
           {
               "name": "get_weather",
               "description": "Get weather information",
               "parameters": {
                   "type": "object",
                   "properties": {
                       "location": {
                           "type": "string",
                           "description": "City name"
                       }
                   }
               }
           }
       ]
   }

   // Example OpenAI Chat Response
   {
       "id": "chatcmpl-123",
       "object": "chat.completion",
       "created": 1677652288,
       "choices": [{
           "index": 0,
           "message": {
               "role": "assistant",
               "content": "Hello! How can I help you today?"
           },
           "finish_reason": "stop"
       }],
       "usage": {
           "prompt_tokens": 9,
           "completion_tokens": 12,
           "total_tokens": 21
       }
   }
   ```

2. **Provider-Specific JSON Mappings**
   ```java
   // OpenAI specific request mapping
   public class OpenAIRequest {
       @JsonProperty("model")
       private String model;
       
       @JsonProperty("messages")
       private List<Message> messages;
       
       @JsonProperty("functions")
       private List<FunctionSchema> functions;
       
       @JsonProperty("temperature")
       private Double temperature;
       
       // Other OpenAI specific fields
   }

   // ChatGLM specific request mapping
   public class ChatGLMRequest {
       @JsonProperty("prompt")
       private String prompt;
       
       @JsonProperty("history")
       private List<List<String>> history;
       
       @JsonProperty("temperature")
       private Double temperature;
   }
   ```

3. **JSON Conversion Utilities**
   ```java
   public class JsonConverter {
       private final ObjectMapper mapper;
       
       public String toJson(Object obj) throws JsonProcessingException {
           return mapper.writeValueAsString(obj);
       }
       
       public <T> T fromJson(String json, Class<T> type) throws JsonProcessingException {
           return mapper.readValue(json, type);
       }
       
       // Provider-specific conversions
       public String convertToProviderFormat(ChatRequest request, String provider) {
           return switch (provider) {
               case "openai" -> convertForOpenAI(request);
               case "chatglm" -> convertForChatGLM(request);
               case "ollama" -> convertForOllama(request);
               default -> throw new IllegalArgumentException("Unsupported provider");
           };
       }
   }
   ```

4. **Common JSON Fields Across Providers**
   ```java
   public interface CommonFields {
       String MODEL = "model";
       String MESSAGES = "messages";
       String TEMPERATURE = "temperature";
       String MAX_TOKENS = "max_tokens";
       String STOP = "stop";
       String STREAM = "stream";
   }
   ```

5. **JSON Response Parsing**
   ```java
   public class ResponseParser {
       private final ObjectMapper mapper;
       
       public ChatResponse parse(String jsonResponse, String provider) throws LLMException {
           try {
               JsonNode root = mapper.readTree(jsonResponse);
               
               // Common fields extraction
               String content = extractContent(root, provider);
               List<ToolCall> toolCalls = extractToolCalls(root, provider);
               Map<String, Object> usage = extractUsage(root, provider);
               
               return new ChatResponse(
                   extractId(root, provider),
                   content,
                   toolCalls,
                   usage
               );
           } catch (JsonProcessingException e) {
               throw new LLMException("Failed to parse response", e);
           }
       }
   }
   ```