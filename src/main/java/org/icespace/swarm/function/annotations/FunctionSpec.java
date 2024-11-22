package org.icespace.swarm.function.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying function metadata for LLM function calls.
 * The function name will be automatically derived from the method name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FunctionSpec {
    /**
     * Description of what the function does.
     */
    String description();
}