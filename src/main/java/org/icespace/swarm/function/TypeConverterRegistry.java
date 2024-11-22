package org.icespace.swarm.function;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for type converters.
 */
public class TypeConverterRegistry {
    private final Map<Class<?>, TypeConverter> converters = new HashMap<>();

    public TypeConverterRegistry() {
        // Register default converters
        registerDefaultConverters();
    }

    public void registerConverter(Class<?> type, TypeConverter converter) {
        converters.put(type, converter);
    }

    public TypeConverter getConverter(Class<?> type) {
        return converters.get(type);
    }

    private void registerDefaultConverters() {
        // String converter
        registerConverter(String.class, new TypeConverter() {
            @Override
            public boolean canConvert(Class<?> from, Class<?> to) {
                return to == String.class;
            }

            @Override
            public Object convert(Object value, Class<?> targetType) {
                if (value == null) {
                    return null;
                }
                return value.toString();
            }

            @Override
            public String toJsonSchemaType(Class<?> javaType) {
                return "string";
            }
        });

        // Number converters
        registerConverter(Integer.class, new TypeConverter() {
            @Override
            public boolean canConvert(Class<?> from, Class<?> to) {
                return Number.class.isAssignableFrom(from) && to == Integer.class;
            }

            @Override
            public Object convert(Object value, Class<?> targetType) {
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
                return Integer.parseInt(value.toString());
            }

            @Override
            public String toJsonSchemaType(Class<?> javaType) {
                return "integer";
            }
        });

        // Add more default converters as needed
    }
}