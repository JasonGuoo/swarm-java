package org.icespace.swarm.function;

/**
 * Interface for type conversion between JSON and Java types.
 */
public interface TypeConverter {
    /**
     * Check if this converter can handle the conversion
     */
    boolean canConvert(Class<?> from, Class<?> to);

    /**
     * Convert a value to the target type
     */
    Object convert(Object value, Class<?> targetType);

    /**
     * Get the JSON Schema type for a Java type
     */
    String toJsonSchemaType(Class<?> javaType);
}