package org.icespace.swarm.util;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Environment {
    private static final Logger log = LoggerFactory.getLogger(Environment.class);
    private static final Dotenv dotenv;

    static {
        dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
    }

    public static String get(String key) {
        String value = dotenv.get(key);
        if (value == null) {
            value = System.getenv(key);
        }
        return value;
    }

    public static String getRequired(String key) {
        String value = get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required environment variable " + key + " is not set");
        }
        return value;
    }

    public static String getLLMProvider() {
        return getRequired("LLM_PROVIDER").toLowerCase();
    }
} 