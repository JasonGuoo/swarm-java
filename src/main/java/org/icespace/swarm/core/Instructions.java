package org.icespace.swarm.core;

import java.util.Map;

/**
 * Interface for generating agent instructions.
 */
public interface Instructions {
    /**
     * Generate instructions based on context
     */
    String generate(Map<String, Object> context);
}

// Static implementation
class StaticInstructions implements Instructions {
    private final String instructions;

    public StaticInstructions(String instructions) {
        this.instructions = instructions;
    }

    @Override
    public String generate(Map<String, Object> context) {
        return instructions;
    }
}

// Dynamic implementation
class DynamicInstructions implements Instructions {
    private final String template;

    public DynamicInstructions(String template) {
        this.template = template;
    }

    @Override
    public String generate(Map<String, Object> context) {
        String result = template;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}",
                    String.valueOf(entry.getValue()));
        }
        return result;
    }
}