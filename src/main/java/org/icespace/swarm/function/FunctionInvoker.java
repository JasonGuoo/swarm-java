package org.icespace.swarm.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.icespace.swarm.function.annotations.FunctionSpec;
import org.icespace.swarm.function.annotations.Parameter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Handles dynamic function invocation with parameter conversion and validation.
 */
public class FunctionInvoker {
    private final ObjectMapper objectMapper;
    private final TypeConverterRegistry typeConverterRegistry;

    public FunctionInvoker() {
        this.objectMapper = new ObjectMapper();
        this.typeConverterRegistry = new TypeConverterRegistry();
    }

    /**
     * Invokes a function with the given parameters
     */
    public Object invoke(Method method, Object target, Map<String, Object> parameters) throws Exception {
        // Validate function annotation
        FunctionSpec spec = method.getAnnotation(FunctionSpec.class);
        if (spec == null) {
            throw new IllegalArgumentException("Method must be annotated with @FunctionSpec");
        }

        // Process parameters
        Object[] args = processParameters(method, parameters);

        // Invoke the method
        try {
            return method.invoke(target, args);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) cause;
            }
            throw e;
        }
    }

    private Object[] processParameters(Method method, Map<String, Object> parameters) {
        java.lang.reflect.Parameter[] methodParams = method.getParameters();
        Object[] args = new Object[methodParams.length];

        for (int i = 0; i < methodParams.length; i++) {
            java.lang.reflect.Parameter param = methodParams[i];
            Parameter annotation = param.getAnnotation(Parameter.class);
            
            String paramName = param.getName();
            Class<?> paramType = param.getType();
            Object rawValue = parameters.get(paramName);

            // Handle missing required parameters
            if (rawValue == null) {
                if (annotation != null && !annotation.defaultValue().isEmpty()) {
                    rawValue = annotation.defaultValue();
                } else if (!paramType.isPrimitive()) {
                    // Allow null for non-primitive types unless explicitly marked as required
                    args[i] = null;
                    continue;
                } else {
                    throw new IllegalArgumentException(
                            String.format("Required parameter '%s' of type '%s' is missing", paramName, paramType.getSimpleName())
                    );
                }
            }

            try {
                // Convert parameter to the correct type
                args[i] = convertValue(rawValue, paramType);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        String.format("Failed to convert parameter '%s' from '%s' to type '%s': %s",
                                paramName, rawValue, paramType.getSimpleName(), e.getMessage())
                );
            }
        }

        return args;
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            if (targetType.isPrimitive()) {
                throw new IllegalArgumentException("Cannot convert null to primitive type " + targetType);
            }
            return null;
        }

        // Handle arrays to collection conversion
        if (Collection.class.isAssignableFrom(targetType) && value.getClass().isArray()) {
            return Arrays.asList((Object[]) value);
        }

        // Handle primitive types
        if (targetType.isPrimitive()) {
            return convertPrimitive(value, targetType);
        }

        // Try to find a converter
        TypeConverter converter = typeConverterRegistry.getConverter(targetType);
        if (converter != null) {
            return converter.convert(value, targetType);
        }

        // Handle string to number conversion
        if (Number.class.isAssignableFrom(targetType) && value instanceof String) {
            try {
                if (targetType == Double.class) {
                    return Double.valueOf((String) value);
                } else if (targetType == Integer.class) {
                    return Integer.valueOf((String) value);
                } else if (targetType == Long.class) {
                    return Long.valueOf((String) value);
                } else if (targetType == Float.class) {
                    return Float.valueOf((String) value);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot convert string '" + value + "' to " + targetType.getSimpleName());
            }
        }

        // Default conversion using Jackson
        try {
            return objectMapper.convertValue(value, targetType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("Cannot convert value of type '%s' to target type '%s'",
                            value.getClass().getSimpleName(), targetType.getSimpleName())
            );
        }
    }

    private Object convertPrimitive(Object value, Class<?> targetType) {
        String strValue = value.toString();
        
        try {
            if (targetType == int.class) return Integer.parseInt(strValue);
            if (targetType == long.class) return Long.parseLong(strValue);
            if (targetType == double.class) return Double.parseDouble(strValue);
            if (targetType == float.class) return Float.parseFloat(strValue);
            if (targetType == boolean.class) return Boolean.parseBoolean(strValue);
            if (targetType == byte.class) return Byte.parseByte(strValue);
            if (targetType == short.class) return Short.parseShort(strValue);
            if (targetType == char.class && strValue.length() > 0) return strValue.charAt(0);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Cannot convert value '%s' to primitive type '%s'",
                            strValue, targetType.getSimpleName())
            );
        }
        
        throw new IllegalArgumentException("Unsupported primitive type: " + targetType);
    }
}