package org.icespace.swarm.core;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ToolChoice {
    AUTO("auto"),
    NONE("none");

    private final String value;

    ToolChoice(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ToolChoice fromValue(String value) {
        for (ToolChoice choice : values()) {
            if (choice.value.equals(value)) {
                return choice;
            }
        }
        return null;
    }
}