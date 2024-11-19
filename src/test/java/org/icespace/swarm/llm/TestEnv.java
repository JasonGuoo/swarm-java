package org.icespace.swarm.llm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestEnv {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = TestEnv.class.getResourceAsStream("/.env")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            System.err.println("Warning: .env file not found or cannot be read");
        }
    }

    public static String get(String key) {
        // First try system environment
        String value = System.getenv(key);
        if (value != null) {
            return value;
        }
        // Then try .env file
        return props.getProperty(key);
    }

    public static String require(String key) {
        String value = get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Required test environment variable '" + key + "' is not set. " +
                            "Please copy .env.template to .env and set your values.");
        }
        return value;
    }
}