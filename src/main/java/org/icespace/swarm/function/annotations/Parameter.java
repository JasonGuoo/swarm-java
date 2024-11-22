package org.icespace.swarm.function.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying parameter metadata.
 * The parameter name and type will be automatically derived from the method parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Parameter {
    /**
     * Description of what the parameter does.
     */
    String description();

    /**
     * Default value for the parameter if not provided.
     * Should be a string representation of the value compatible with the parameter type.
     */
    String defaultValue() default "";
}