package org.icespace.swarm.function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class FunctionInvokerTest {
    private FunctionInvoker invoker;
    private TestFunctions testFunctions;

    @BeforeEach
    void setUp() {
        invoker = new FunctionInvoker();
        testFunctions = new TestFunctions();
    }

    @Nested
    class BasicInvocation {
        @Test
        void testSimpleNumberAddition() throws Exception {
            Method method = TestFunctions.class.getMethod("add", Double.class, Double.class);
            Map<String, Object> params = new HashMap<>();
            params.put("a", 5);
            params.put("b", 3);

            Object result = invoker.invoke(method, testFunctions, params);
            assertEquals(8.0, result);
        }

        @Test
        void testGreetingWithDefaults() throws Exception {
            Method method = TestFunctions.class.getMethod("greet", String.class, Boolean.class);
            Map<String, Object> params = new HashMap<>();
            params.put("name", "John");

            Object result = invoker.invoke(method, testFunctions, params);
            assertEquals("Hi John", result);
        }

        @Test
        void testGreetingWithFormal() throws Exception {
            Method method = TestFunctions.class.getMethod("greet", String.class, Boolean.class);
            Map<String, Object> params = new HashMap<>();
            params.put("name", "John");
            params.put("formal", true);

            Object result = invoker.invoke(method, testFunctions, params);
            assertEquals("Dear John", result);
        }
    }

    @Nested
    class ComplexTypes {
        @Test
        void testListProcessing() throws Exception {
            Method method = TestFunctions.class.getMethod("listItems", List.class, String.class);
            Map<String, Object> params = new HashMap<>();
            params.put("items", Arrays.asList("apple", "banana", "orange"));
            params.put("prefix", "* ");

            @SuppressWarnings("unchecked")
            List<String> result = (List<String>) invoker.invoke(method, testFunctions, params);
            assertEquals(3, result.size());
            assertTrue(result.get(0).startsWith("* "));
        }

        @Test
        void testMapProcessing() throws Exception {
            Method method = TestFunctions.class.getMethod("processData", Map.class);
            Map<String, Object> data = new HashMap<>();
            data.put("key", "value");
            Map<String, Object> params = new HashMap<>();
            params.put("data", data);

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) invoker.invoke(method, testFunctions, params);
            assertTrue((Boolean) result.get("processed"));
        }
    }

    @Nested
    class ValidationTests {
        @Test
        void testMissingRequiredParameter() throws NoSuchMethodException {
            Method method = TestFunctions.class.getMethod("add", Double.class, Double.class);
            Map<String, Object> params = new HashMap<>();
            params.put("a", 5.0);
            // Missing parameter 'b'

            assertThrows(IllegalArgumentException.class,
                    () -> invoker.invoke(method, testFunctions, params));
        }

        @Test
        void testInvalidParameterType() throws NoSuchMethodException {
            Method method = TestFunctions.class.getMethod("add", Double.class, Double.class);
            Map<String, Object> params = new HashMap<>();
            params.put("a", "not a number");
            params.put("b", 3.0);

            assertThrows(Exception.class,
                    () -> invoker.invoke(method, testFunctions, params));
        }
    }

    @Nested
    class TypeConversionTests {
        @Test
        void testStringToNumber() throws Exception {
            Method method = TestFunctions.class.getMethod("add", Double.class, Double.class);
            Map<String, Object> params = new HashMap<>();
            params.put("a", "5.0");
            params.put("b", "3.0");

            Object result = invoker.invoke(method, testFunctions, params);
            assertEquals(8.0, result);
        }

        @Test
        void testIntegerToDouble() throws Exception {
            Method method = TestFunctions.class.getMethod("add", Double.class, Double.class);
            Map<String, Object> params = new HashMap<>();
            params.put("a", 5);
            params.put("b", 3);

            Object result = invoker.invoke(method, testFunctions, params);
            assertEquals(8.0, result);
        }
    }

    @Nested
    class NullHandlingTests {
        @Test
        void testNullParameterWithDefault() throws Exception {
            Method method = TestFunctions.class.getMethod("greet", String.class, Boolean.class);
            Map<String, Object> params = new HashMap<>();
            params.put("name", null); // null name

            assertThrows(IllegalArgumentException.class,
                    () -> invoker.invoke(method, testFunctions, params));
        }

        @Test
        void testNullableParameter() throws Exception {
            Method method = TestFunctions.class.getMethod("listItems", List.class, String.class);
            Map<String, Object> params = new HashMap<>();
            params.put("items", Arrays.asList("item1", null, "item3")); // null item in list
            params.put("prefix", "- ");

            List<String> result = (List<String>) invoker.invoke(method, testFunctions, params);
            assertEquals(3, result.size());
            assertTrue(result.get(1).endsWith("null"));
        }
    }

    @Nested
    class CollectionTypeTests {
        @Test
        void testEmptyList() throws Exception {
            Method method = TestFunctions.class.getMethod("listItems", List.class, String.class);
            Map<String, Object> params = new HashMap<>();
            params.put("items", Collections.emptyList());
            params.put("prefix", "- ");

            List<String> result = (List<String>) invoker.invoke(method, testFunctions, params);
            assertTrue(result.isEmpty());
        }

        @Test
        void testArrayToList() throws Exception {
            Method method = TestFunctions.class.getMethod("listItems", List.class, String.class);
            Map<String, Object> params = new HashMap<>();
            params.put("items", new String[] { "item1", "item2" }); // Array instead of List
            params.put("prefix", "- ");

            List<String> result = (List<String>) invoker.invoke(method, testFunctions, params);
            assertEquals(2, result.size());
        }
    }

    @Nested
    class ConcurrencyTests {
        @Test
        void testConcurrentInvocation() throws Exception {
            Method method = TestFunctions.class.getMethod("add", Double.class, Double.class);
            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicReference<Exception> error = new AtomicReference<>();

            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                final int num = i;
                Thread thread = new Thread(() -> {
                    try {
                        Map<String, Object> params = new HashMap<>();
                        params.put("a", num);
                        params.put("b", 1);
                        invoker.invoke(method, testFunctions, params);
                        latch.countDown();
                    } catch (Exception e) {
                        error.set(e);
                    }
                });
                threads.add(thread);
            }

            threads.forEach(Thread::start);
            latch.await(5, TimeUnit.SECONDS);

            assertNull(error.get(), "No errors should occur during concurrent invocation");
        }
    }

    @Nested
    class CalculationTests {
        @Test
        void testAddition() throws Exception {
            Method method = TestFunctions.class.getMethod("add", Double.class, Double.class);
            Map<String, Object> params = new HashMap<>();
            params.put("a", 5.2);
            params.put("b", 3.8);

            Object result = invoker.invoke(method, testFunctions, params);
            assertEquals(9.0, (Double) result, 0.001);
        }

        @Test
        void testMultiplication() throws Exception {
            Method method = TestFunctions.class.getMethod("multiply", Double.class, Double.class);
            Map<String, Object> params = new HashMap<>();
            params.put("a", 4.0);
            params.put("b", 2.5);

            Object result = invoker.invoke(method, testFunctions, params);
            assertEquals(10.0, (Double) result, 0.001);
        }

        @Test
        void testCalculateStats() throws Exception {
            Method method = TestFunctions.class.getMethod("calculateStats", List.class);
            Map<String, Object> params = new HashMap<>();
            params.put("numbers", Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0));

            @SuppressWarnings("unchecked")
            Map<String, Double> result = (Map<String, Double>) invoker.invoke(method, testFunctions, params);

            assertEquals(15.0, result.get("sum"), 0.001);
            assertEquals(3.0, result.get("average"), 0.001);
            assertEquals(5.0, result.get("max"), 0.001);
            assertEquals(1.0, result.get("min"), 0.001);
        }
    }

    @Nested
    class TextProcessingTests {
        @Test
        void testConcatenation() throws Exception {
            Method method = TestFunctions.class.getMethod("concatenate", List.class, String.class);
            Map<String, Object> params = new HashMap<>();
            params.put("strings", Arrays.asList("Hello", "World", "!"));
            params.put("separator", ", ");

            Object result = invoker.invoke(method, testFunctions, params);
            assertEquals("Hello, World, !", result);
        }

        @Test
        void testFormatText() throws Exception {
            Method method = TestFunctions.class.getMethod("formatText", String.class, Map.class);
            Map<String, Object> params = new HashMap<>();
            params.put("template", "Hello, {name}! How is the {weather}?");
            params.put("variables", Map.of(
                    "name", "Alice",
                    "weather", "sunshine"));

            Object result = invoker.invoke(method, testFunctions, params);
            assertEquals("Hello, Alice! How is the sunshine?", result);
        }

        @Test
        void testProcessText() throws Exception {
            Method method = TestFunctions.class.getMethod("processText", String.class, List.class);
            Map<String, Object> params = new HashMap<>();
            params.put("text", "  Hello World  ");
            params.put("operations", Arrays.asList("trim", "uppercase", "reverse"));

            Object result = invoker.invoke(method, testFunctions, params);
            assertEquals("DLROW OLLEH", result);
        }
    }
}