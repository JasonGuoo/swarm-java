package org.icespace.swarm.function;

import org.icespace.swarm.function.annotations.FunctionSpec;
import org.icespace.swarm.function.annotations.Parameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DynamicFunctionDiscoveryTest {
    private FunctionInvoker invoker;

    @BeforeEach
    void setUp() {
        invoker = new FunctionInvoker();
    }

    static class DynamicObject {
        @FunctionSpec(description = "Say hello to someone")
        public String sayHello(
                @Parameter(description = "Name to greet") String name) {
            return "Hello, " + name + "!";
        }

        @FunctionSpec(description = "Perform calculation")
        public Double calculate(
                @Parameter(description = "Operation to perform") String operation,
                @Parameter(description = "Numbers to calculate") List<Double> numbers) {
            switch (operation.toLowerCase()) {
                case "sum":
                    return numbers.stream().mapToDouble(Double::doubleValue).sum();
                case "average":
                    return numbers.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                case "max":
                    return numbers.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                case "min":
                    return numbers.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                default:
                    throw new IllegalArgumentException("Unknown operation: " + operation);
            }
        }

        // Should not be discovered (no annotation)
        public String internalMethod() {
            return "internal";
        }
    }

    @Test
    void discoverFunctions() {
        DynamicObject obj = new DynamicObject();
        List<Method> functions = Arrays.stream(obj.getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(FunctionSpec.class))
                .collect(Collectors.toList());

        // Print discovered function information
        for (Method method : functions) {
            FunctionSpec spec = method.getAnnotation(FunctionSpec.class);
            System.out.println("\nDiscovered function:");
            System.out.println("Name: " + method.getName());
            System.out.println("Description: " + spec.description());

            System.out.println("Parameters:");
            for (java.lang.reflect.Parameter param : method.getParameters()) {
                Parameter annotation = param.getAnnotation(Parameter.class);
                if (annotation != null) {
                    System.out.println("  - " + param.getName() + ": " + annotation.description());
                }
            }
        }

        // Verify function discovery
        assertEquals(2, functions.size());
        assertTrue(functions.stream().anyMatch(m -> m.getName().equals("sayHello")));
        assertTrue(functions.stream().anyMatch(m -> m.getName().equals("calculate")));
    }

    @Test
    void invokeFunction() {
        DynamicObject obj = new DynamicObject();
        Method sayHello = Arrays.stream(obj.getClass().getMethods())
                .filter(m -> m.getName().equals("sayHello"))
                .findFirst()
                .orElseThrow();

        Map<String, Object> args = new HashMap<>();
        args.put("name", "World");

        try {
            Object result = invoker.invoke(sayHello, obj, args);
            assertEquals("Hello, World!", result);
        } catch (Exception e) {
            fail("Failed to invoke function", e);
        }
    }

    @Test
    void calculateFunction() {
        DynamicObject obj = new DynamicObject();
        Method calculate = Arrays.stream(obj.getClass().getMethods())
                .filter(m -> m.getName().equals("calculate"))
                .findFirst()
                .orElseThrow();

        Map<String, Object> args = new HashMap<>();
        args.put("operation", "sum");
        args.put("numbers", Arrays.asList(1.0, 2.0, 3.0));

        try {
            Object result = invoker.invoke(calculate, obj, args);
            assertEquals(6.0, result);
        } catch (Exception e) {
            fail("Failed to invoke function", e);
        }
    }
}