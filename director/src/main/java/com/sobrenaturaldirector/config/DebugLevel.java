package com.sobrenaturaldirector.config;

public enum DebugLevel {
    OFF,
    ERROR,
    INFO,
    DECISIONS,
    VERBOSE;

    public static DebugLevel parse(String value) {
        if (value == null) return INFO;
        try {
            return valueOf(value.trim().toUpperCase(java.util.Locale.ENGLISH));
        } catch (IllegalArgumentException exception) {
            return INFO;
        }
    }
}
