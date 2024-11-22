package org.icespace.swarm.function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TypeConverterRegistryTest {
    private TypeConverterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new TypeConverterRegistry();
    }

    @Nested
    class StringConversion {
        @Test
        void testStringToString() {
            TypeConverter converter = registry.getConverter(String.class);
            assertNotNull(converter);
            assertEquals("test", converter.convert("test", String.class));
        }

        @Test
        void testNumberToString() {
            TypeConverter converter = registry.getConverter(String.class);
            assertNotNull(converter);
            assertEquals("42", converter.convert(42, String.class));
        }
    }

    @Nested
    class NumberConversion {
        @Test
        void testStringToInteger() {
            TypeConverter converter = registry.getConverter(Integer.class);
            assertNotNull(converter);
            assertEquals(42, converter.convert("42", Integer.class));
        }

        @Test
        void testDoubleToInteger() {
            TypeConverter converter = registry.getConverter(Integer.class);
            assertNotNull(converter);
            assertEquals(42, converter.convert(42.0, Integer.class));
        }
    }

    @Test
    void testCustomConverterRegistration() {
        TypeConverter booleanConverter = new TypeConverter() {
            @Override
            public boolean canConvert(Class<?> from, Class<?> to) {
                return to == Boolean.class;
            }

            @Override
            public Object convert(Object value, Class<?> targetType) {
                if (value instanceof String) {
                    return "true".equalsIgnoreCase((String) value) || "yes".equalsIgnoreCase((String) value);
                }
                return false;
            }

            @Override
            public String toJsonSchemaType(Class<?> javaType) {
                return "boolean";
            }
        };

        registry.registerConverter(Boolean.class, booleanConverter);

        TypeConverter converter = registry.getConverter(Boolean.class);
        assertNotNull(converter);
        assertTrue((Boolean) converter.convert("yes", Boolean.class));
        assertFalse((Boolean) converter.convert("no", Boolean.class));
    }

    @Nested
    class ComplexTypeConversion {
        @Test
        void testListConversion() {
            TypeConverter converter = new TypeConverter() {
                @Override
                public boolean canConvert(Class<?> from, Class<?> to) {
                    return List.class.isAssignableFrom(to);
                }

                @Override
                public Object convert(Object value, Class<?> targetType) {
                    if (value instanceof String) {
                        return Arrays.asList(((String) value).split(","));
                    }
                    return null;
                }

                @Override
                public String toJsonSchemaType(Class<?> javaType) {
                    return "array";
                }
            };

            registry.registerConverter(List.class, converter);

            TypeConverter registeredConverter = registry.getConverter(List.class);
            assertNotNull(registeredConverter);

            List<?> result = (List<?>) registeredConverter.convert("a,b,c", List.class);
            assertEquals(3, result.size());
            assertEquals("b", result.get(1));
        }

        @Test
        void testMapConversion() {
            TypeConverter converter = new TypeConverter() {
                @Override
                public boolean canConvert(Class<?> from, Class<?> to) {
                    return Map.class.isAssignableFrom(to);
                }

                @Override
                public Object convert(Object value, Class<?> targetType) {
                    if (value instanceof String) {
                        Map<String, String> map = new HashMap<>();
                        String[] pairs = ((String) value).split(";");
                        for (String pair : pairs) {
                            String[] kv = pair.split("=");
                            if (kv.length == 2) {
                                map.put(kv[0], kv[1]);
                            }
                        }
                        return map;
                    }
                    return null;
                }

                @Override
                public String toJsonSchemaType(Class<?> javaType) {
                    return "object";
                }
            };

            registry.registerConverter(Map.class, converter);

            TypeConverter registeredConverter = registry.getConverter(Map.class);
            assertNotNull(registeredConverter);

            Map<?, ?> result = (Map<?, ?>) registeredConverter.convert("key1=value1;key2=value2", Map.class);
            assertEquals(2, result.size());
            assertEquals("value2", result.get("key2"));
        }
    }

    @Nested
    class EdgeCaseTests {
        @Test
        void testNullValueConversion() {
            TypeConverter converter = registry.getConverter(String.class);
            assertNull(converter.convert(null, String.class));
        }

        @Test
        void testEmptyStringConversion() {
            TypeConverter converter = registry.getConverter(String.class);
            assertEquals("", converter.convert("", String.class));
        }
    }
}