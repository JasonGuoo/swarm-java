package org.icespace.swarm.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the result of a function execution, including any context
 * updates.
 */
public class Result {
    private final Object value;
    private final Map<String, Object> contextUpdates;

    public Result(Object value) {
        this.value = value;
        this.contextUpdates = new HashMap<>();
    }

    public Result withContextUpdate(String key, Object value) {
        contextUpdates.put(key, value);
        return this;
    }

    public Result withContextUpdates(Map<String, Object> updates) {
        contextUpdates.putAll(updates);
        return this;
    }

    public Object getValue() {
        return value;
    }

    public Map<String, Object> getContextUpdates() {
        return new HashMap<>(contextUpdates);
    }

    public boolean hasContextUpdates() {
        return !contextUpdates.isEmpty();
    }
}