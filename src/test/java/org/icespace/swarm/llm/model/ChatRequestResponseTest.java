package org.icespace.swarm.llm.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ChatRequestResponseTest {
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    class ChatRequestTests {
        @Test
        void testBasicChatRequest() throws Exception {
            ChatRequest request = ChatRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(List.of(
                            Message.builder()
                                    .role("user")
                                    .content("Hello!")
                                    .build()))
                    .temperature(0.7)
                    .build();

            String json = objectMapper.writeValueAsString(request);

            assertTrue(json.contains("\"model\":\"gpt-3.5-turbo\""));
            assertTrue(json.contains("\"messages\":[{\"role\":\"user\",\"content\":\"Hello!\"}]"));
            assertTrue(json.contains("\"temperature\":0.7"));

            ChatRequest deserializedRequest = objectMapper.readValue(json, ChatRequest.class);
            assertEquals(request.getModel(), deserializedRequest.getModel());
            assertEquals(request.getMessages().get(0).getContent(),
                    deserializedRequest.getMessages().get(0).getContent());
        }

        @Test
        void testChatRequestWithFunctions() throws Exception {
            FunctionSchema weatherFunction = FunctionSchema.builder()
                    .name("get_weather")
                    .description("Get weather information")
                    .parameters(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "location", Map.of(
                                            "type", "string",
                                            "description", "City name"))))
                    .build();

            ChatRequest request = ChatRequest.builder()
                    .model("gpt-4")
                    .messages(List.of(
                            Message.builder()
                                    .role("user")
                                    .content("What's the weather?")
                                    .build()))
                    .functions(List.of(weatherFunction))
                    .functionCall("auto")
                    .build();

            String json = objectMapper.writeValueAsString(request);

            assertTrue(json.contains("\"functions\":[{\"name\":\"get_weather\""));
            assertTrue(json.contains("\"function_call\":\"auto\""));

            ChatRequest deserializedRequest = objectMapper.readValue(json, ChatRequest.class);
            assertEquals(1, deserializedRequest.getFunctions().size());
            assertEquals("get_weather", deserializedRequest.getFunctions().get(0).getName());
        }

        @Test
        void testChatRequestWithAllParameters() throws Exception {
            ChatRequest request = ChatRequest.builder()
                    .model("gpt-4")
                    .messages(List.of(
                            Message.builder()
                                    .role("system")
                                    .content("You are a helpful assistant")
                                    .build(),
                            Message.builder()
                                    .role("user")
                                    .content("Hi")
                                    .build()))
                    .temperature(0.7)
                    .topP(1.0)
                    .n(1)
                    .stream(false)
                    .stop(List.of("\n", "Stop"))
                    .maxTokens(100)
                    .presencePenalty(0.0)
                    .frequencyPenalty(0.0)
                    .logitBias(Map.of("50256", -100))
                    .user("user123")
                    .build();

            String json = objectMapper.writeValueAsString(request);
            ChatRequest deserialized = objectMapper.readValue(json, ChatRequest.class);

            assertEquals(request.getTemperature(), deserialized.getTemperature());
            assertEquals(request.getMaxTokens(), deserialized.getMaxTokens());
            assertEquals(request.getLogitBias(), deserialized.getLogitBias());
        }
    }

    @Nested
    class ChatResponseTests {
        @Test
        void testBasicChatResponse() throws Exception {
            String json = "{\n" +
                    "    \"id\": \"chatcmpl-123\",\n" +
                    "    \"object\": \"chat.completion\",\n" +
                    "    \"created\": 1677652288,\n" +
                    "    \"choices\": [{\n" +
                    "        \"index\": 0,\n" +
                    "        \"message\": {\n" +
                    "            \"role\": \"assistant\",\n" +
                    "            \"content\": \"Hello! How can I help you today?\"\n" +
                    "        },\n" +
                    "        \"finish_reason\": \"stop\"\n" +
                    "    }],\n" +
                    "    \"usage\": {\n" +
                    "        \"prompt_tokens\": 9,\n" +
                    "        \"completion_tokens\": 12,\n" +
                    "        \"total_tokens\": 21\n" +
                    "    }\n" +
                    "}";

            ChatResponse response = objectMapper.readValue(json, ChatResponse.class);

            assertEquals("chatcmpl-123", response.getId());
            assertEquals(1, response.getChoices().size());
            assertEquals("Hello! How can I help you today?",
                    response.getChoices().get(0).getMessage().getContent());
            assertEquals(21, response.getUsage().getTotalTokens());
        }

        @Test
        void testChatResponseWithFunctionCall() throws Exception {
            String json = "{\n" +
                    "    \"id\": \"chatcmpl-123\",\n" +
                    "    \"object\": \"chat.completion\",\n" +
                    "    \"created\": 1677652288,\n" +
                    "    \"choices\": [{\n" +
                    "        \"index\": 0,\n" +
                    "        \"message\": {\n" +
                    "            \"role\": \"assistant\",\n" +
                    "            \"content\": null,\n" +
                    "            \"tool_calls\": [{\n" +
                    "                \"id\": \"call_123\",\n" +
                    "                \"type\": \"function\",\n" +
                    "                \"function\": {\n" +
                    "                    \"name\": \"get_weather\",\n" +
                    "                    \"arguments\": \"{\\\"location\\\": \\\"London\\\"}\"\n" +
                    "                }\n" +
                    "            }]\n" +
                    "        },\n" +
                    "        \"finish_reason\": \"tool_calls\"\n" +
                    "    }],\n" +
                    "    \"usage\": {\n" +
                    "        \"prompt_tokens\": 9,\n" +
                    "        \"completion_tokens\": 12,\n" +
                    "        \"total_tokens\": 21\n" +
                    "    }\n" +
                    "}";

            ChatResponse response = objectMapper.readValue(json, ChatResponse.class);

            assertNotNull(response.getChoices().get(0).getMessage().getToolCalls());
            assertEquals("get_weather",
                    response.getChoices().get(0).getMessage().getToolCalls()[0]
                            .getFunction().getName());
            assertEquals("tool_calls", response.getChoices().get(0).getFinishReason());
        }
    }

    @Nested
    class MessageTests {
        @Test
        void testMessageWithToolCalls() throws Exception {
            Message message = Message.builder()
                    .role("assistant")
                    .content(null)
                    .toolCalls(new ToolCall[] {
                            new ToolCall() {
                                {
                                    setId("call_123");
                                    setType("function");
                                    setFunction(new FunctionCall() {
                                        {
                                            setName("get_weather");
                                            setArguments("{\"location\": \"London\"}");
                                        }
                                    });
                                }
                            }
                    })
                    .build();

            String json = objectMapper.writeValueAsString(message);
            Message deserialized = objectMapper.readValue(json, Message.class);

            assertNotNull(deserialized.getToolCalls());
            assertEquals("get_weather", deserialized.getToolCalls()[0].getFunction().getName());
        }

        @Test
        void testMessageWithName() throws Exception {
            Message message = Message.builder()
                    .role("assistant")
                    .content("Hello")
                    .name("weather_bot")
                    .build();

            String json = objectMapper.writeValueAsString(message);
            Message deserialized = objectMapper.readValue(json, Message.class);

            assertEquals("weather_bot", deserialized.getName());
        }
    }

    @Nested
    class FunctionSchemaTests {
        @Test
        void testFunctionSchemaWithComplexParameters() throws Exception {
            FunctionSchema schema = FunctionSchema.builder()
                    .name("search_hotels")
                    .description("Search for hotels")
                    .parameters(Map.of(
                            "type", "object",
                            "required", List.of("location", "check_in"),
                            "properties", Map.of(
                                    "location", Map.of(
                                            "type", "string",
                                            "description", "City name"),
                                    "check_in", Map.of(
                                            "type", "string",
                                            "format", "date",
                                            "description", "Check-in date"),
                                    "stars", Map.of(
                                            "type", "integer",
                                            "minimum", 1,
                                            "maximum", 5,
                                            "description", "Hotel rating"))))
                    .build();

            String json = objectMapper.writeValueAsString(schema);
            FunctionSchema deserialized = objectMapper.readValue(json, FunctionSchema.class);

            assertEquals(schema.getName(), deserialized.getName());
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) deserialized.getParameters().get("properties");
            assertNotNull(properties.get("location"));
            assertNotNull(properties.get("check_in"));
            assertNotNull(properties.get("stars"));
        }
    }

    @Nested
    class FlexibleJsonTests {
        @Test
        void testResponseWithMissingFields() throws Exception {
            String json = "{\n" +
                    "    \"id\": \"chatcmpl-123\",\n" +
                    "    \"choices\": [{\n" +
                    "        \"message\": {\n" +
                    "            \"content\": \"Hello!\"\n" +
                    "        }\n" +
                    "    }]\n" +
                    "}";

            ChatResponse response = objectMapper.readValue(json, ChatResponse.class);

            assertNotNull(response);
            assertEquals("chatcmpl-123", response.getId());
            assertNull(response.getCreated()); // Missing field should be null
            assertNotNull(response.getChoices());
            assertEquals("Hello!", response.getChoices().get(0).getMessage().getContent());
        }

        @Test
        void testRequestWithExtraFields() throws Exception {
            String json = "{\n" +
                    "    \"model\": \"gpt-4\",\n" +
                    "    \"messages\": [{\"role\": \"user\", \"content\": \"Hi\"}],\n" +
                    "    \"extra_field\": \"some value\",\n" +
                    "    \"unknown_setting\": {\n" +
                    "        \"nested\": \"value\"\n" +
                    "    }\n" +
                    "}";

            ChatRequest request = objectMapper.readValue(json, ChatRequest.class);

            assertNotNull(request);
            assertEquals("gpt-4", request.getModel());
            assertEquals(1, request.getMessages().size());
        }

        @Test
        void testMessageWithProviderSpecificFields() throws Exception {
            String json = "{\n" +
                    "    \"role\": \"assistant\",\n" +
                    "    \"content\": \"Hello\",\n" +
                    "    \"custom_field\": \"value\",\n" +
                    "    \"provider_specific\": {\n" +
                    "        \"some\": \"data\"\n" +
                    "    }\n" +
                    "}";

            Message message = objectMapper.readValue(json, Message.class);

            assertNotNull(message);
            assertEquals("assistant", message.getRole());
            assertEquals("Hello", message.getContent());
        }

        @Test
        void testResponseWithAdditionalUnmappedFields() throws Exception {
            String json = "{\n" +
                    "    \"id\": \"chatcmpl-123\",\n" +
                    "    \"choices\": [{\n" +
                    "        \"message\": {\n" +
                    "            \"content\": \"Hello!\",\n" +
                    "            \"role\": \"assistant\",\n" +
                    "            \"custom_field1\": \"value1\",\n" +
                    "            \"custom_field2\": {\n" +
                    "                \"nested\": \"value2\"\n" +
                    "            }\n" +
                    "        },\n" +
                    "        \"finish_reason\": \"stop\",\n" +
                    "        \"custom_choice_field\": true\n" +
                    "    }],\n" +
                    "    \"custom_response_field\": \"some value\",\n" +
                    "    \"another_field\": 42,\n" +
                    "    \"usage\": {\n" +
                    "        \"total_tokens\": 21,\n" +
                    "        \"custom_usage_field\": \"value3\"\n" +
                    "    }\n" +
                    "}";

            ChatResponse response = objectMapper.readValue(json, ChatResponse.class);

            // First verify standard fields are parsed correctly
            assertNotNull(response);
            assertEquals("chatcmpl-123", response.getId());
            assertEquals("Hello!", response.getChoices().get(0).getMessage().getContent());
            assertEquals("assistant", response.getChoices().get(0).getMessage().getRole());
            assertEquals("stop", response.getChoices().get(0).getFinishReason());
            assertEquals(21, response.getUsage().getTotalTokens());

            // Convert back to JSON to verify field preservation
            String serializedJson = objectMapper.writeValueAsString(response);

            // These assertions should be reversed since we want to preserve all fields
            assertTrue(serializedJson.contains("custom_field1"), "Should preserve message custom fields");
            assertTrue(serializedJson.contains("custom_response_field"), "Should preserve response custom fields");
            assertTrue(serializedJson.contains("custom_usage_field"), "Should preserve usage custom fields");
        }

        @Test
        void testPartialResponseDeserialization() throws Exception {
            String json = "{\n" +
                    "    \"id\": \"chatcmpl-123\",\n" +
                    "    \"choices\": [{\n" +
                    "        \"message\": {\n" +
                    "            \"content\": \"Hello!\",\n" +
                    "            \"role\": \"assistant\"\n" +
                    "        }\n" +
                    "    }]\n" +
                    "}";

            ChatResponse response = objectMapper.readValue(json, ChatResponse.class);

            // Verify that the object is created with available fields
            assertNotNull(response);
            assertEquals("chatcmpl-123", response.getId());
            assertNull(response.getUsage()); // Missing field becomes null
            assertNotNull(response.getChoices());
            assertEquals("Hello!", response.getChoices().get(0).getMessage().getContent());
        }

        @Test
        void testAccessToRawJson() throws Exception {
            String json = "{\n" +
                    "    \"id\": \"chatcmpl-123\",\n" +
                    "    \"custom_field\": \"custom_value\",\n" +
                    "    \"nested_field\": {\n" +
                    "        \"some\": \"value\",\n" +
                    "        \"array\": [1, 2, 3]\n" +
                    "    },\n" +
                    "    \"choices\": [{\n" +
                    "        \"message\": {\n" +
                    "            \"content\": \"Hello!\",\n" +
                    "            \"provider_specific\": {\n" +
                    "                \"priority\": \"high\"\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }]\n" +
                    "}";

            ChatResponse response = objectMapper.readValue(json, ChatResponse.class);

            // Access mapped fields normally
            assertEquals("chatcmpl-123", response.getId());

            // Access unmapped fields through rawJson
            String customField = response.getFieldValue("custom_field", String.class);
            assertNotNull(customField, "custom_field should not be null");
            assertEquals("custom_value", customField);

            String someValue = response.getFieldValue("nested_field/some", String.class);
            assertNotNull(someValue, "nested_field/some should not be null");
            assertEquals("value", someValue);

            Integer arrayValue = response.getFieldValue("nested_field/array/2", Integer.class);
            assertNotNull(arrayValue, "nested_field/array/2 should not be null");
            assertEquals(3, arrayValue);

            // Access nested provider-specific fields
            String priority = response.getFieldValue("choices/0/message/provider_specific/priority", String.class);
            assertNotNull(priority, "provider_specific/priority should not be null");
            assertEquals("high", priority);
        }

        @Test
        void testRawJsonPreservation() throws Exception {
            String json = "{\n" +
                    "    \"id\": \"chatcmpl-123\",\n" +
                    "    \"provider_metadata\": {\n" +
                    "        \"model_version\": \"2.1\",\n" +
                    "        \"latency\": 150,\n" +
                    "        \"custom_data\": {\n" +
                    "            \"region\": \"us-east\",\n" +
                    "            \"datacenter\": \"dc1\"\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";

            ChatResponse response = objectMapper.readValue(json, ChatResponse.class);

            // Verify we can access deeply nested provider-specific data
            Map<?, ?> metadata = (Map<?, ?>) response.getFieldValue("/provider_metadata");
            assertNotNull(metadata);
            assertEquals("2.1", metadata.get("model_version"));
            assertEquals("us-east", ((Map<?, ?>) metadata.get("custom_data")).get("region"));
        }

        @Test
        void testDynamicFieldAccess() throws Exception {
            String json = "{\n" +
                    "    \"id\": \"chatcmpl-123\",\n" +
                    "    \"custom_number\": 42,\n" +
                    "    \"custom_double\": 3.14,\n" +
                    "    \"custom_boolean\": true,\n" +
                    "    \"custom_string\": \"value\",\n" +
                    "    \"custom_array\": [1, \"two\", 3.0],\n" +
                    "    \"custom_object\": {\n" +
                    "        \"nested\": \"value\",\n" +
                    "        \"number\": 123\n" +
                    "    },\n" +
                    "    \"choices\": [{\n" +
                    "        \"message\": {\n" +
                    "            \"content\": \"Hello!\"\n" +
                    "        }\n" +
                    "    }]\n" +
                    "}";

            ChatResponse response = objectMapper.readValue(json, ChatResponse.class);

            // Test automatic type detection
            assertEquals(42, response.getFieldValue("/custom_number"));
            assertEquals(3.14, response.getFieldValue("/custom_double"));
            assertEquals(true, response.getFieldValue("/custom_boolean"));
            assertEquals("value", response.getFieldValue("/custom_string"));

            // Test array access
            List<?> array = (List<?>) response.getFieldValue("/custom_array");
            assertEquals(3, array.size());
            assertEquals(1, array.get(0));
            assertEquals("two", array.get(1));
            assertEquals(3.0, array.get(2));

            // Test object access
            Map<?, ?> object = (Map<?, ?>) response.getFieldValue("/custom_object");
            assertEquals("value", object.get("nested"));
            assertEquals(123, object.get("number"));

            // Test explicit type casting
            Integer number = response.getFieldValue("/custom_number", Integer.class);
            assertEquals(42, number);
            Double decimal = response.getFieldValue("/custom_double", Double.class);
            assertEquals(3.14, decimal);
            String string = response.getFieldValue("/custom_string", String.class);
            assertEquals("value", string);

            // Test type conversion
            Long longNumber = response.getFieldValue("/custom_number", Long.class);
            assertEquals(42L, longNumber);
            Float floatNumber = response.getFieldValue("/custom_double", Float.class);
            assertEquals(3.14f, floatNumber, 0.001);
        }

        @Test
        void testInvalidFieldAccess() throws Exception {
            String json = "{\n" +
                    "    \"id\": \"chatcmpl-123\",\n" +
                    "    \"number_field\": 42\n" +
                    "}";

            ChatResponse response = objectMapper.readValue(json, ChatResponse.class);

            // Test non-existent field
            assertNull(response.getFieldValue("/non_existent"));

            // Test type conversion - number to string should work
            assertNotNull(response.getFieldValue("/number_field", String.class));
            assertEquals("42", response.getFieldValue("/number_field", String.class));

            // Test actually invalid type casting
            assertNull(response.getFieldValue("/id", Integer.class)); // String -> Integer should fail

            // Test null field
            assertNull(response.getFieldValue(null));
            assertNull(response.getFieldValue(null, String.class));
        }

        @Test
        void testNestedFieldAccess() throws Exception {
            String json = "{\n" +
                    "    \"metadata\": {\n" +
                    "        \"provider\": {\n" +
                    "            \"name\": \"custom-llm\",\n" +
                    "            \"version\": \"2.0\",\n" +
                    "            \"settings\": {\n" +
                    "                \"temperature\": 0.7,\n" +
                    "                \"features\": [\"streaming\", \"functions\"],\n" +
                    "                \"limits\": {\n" +
                    "                    \"max_tokens\": 4096,\n" +
                    "                    \"requests_per_min\": 60\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";

            ChatResponse response = objectMapper.readValue(json, ChatResponse.class);

            // Test deep nested access
            assertEquals("custom-llm", response.getFieldValue("/metadata/provider/name"));
            assertEquals(0.7, response.getFieldValue("/metadata/provider/settings/temperature"));

            // Test nested array
            List<?> features = (List<?>) response.getFieldValue("/metadata/provider/settings/features");
            assertEquals("streaming", features.get(0));
            assertEquals("functions", features.get(1));

            // Test nested object with type casting
            Map<?, ?> limits = (Map<?, ?>) response.getFieldValue("/metadata/provider/settings/limits");
            assertEquals(4096, limits.get("max_tokens"));
            assertEquals(60, limits.get("requests_per_min"));
        }
    }
}