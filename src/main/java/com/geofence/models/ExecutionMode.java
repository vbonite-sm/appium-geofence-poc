package com.geofence.models;

/**
 * Test execution modes.
 */
public enum ExecutionMode {
    LOCAL,
    BROWSERSTACK,
    SAUCELABS;

    public boolean isCloud() {
        return this != LOCAL;
    }

    public static ExecutionMode fromString(String value) {
        if (value == null || value.isBlank()) {
            return LOCAL;
        }
        for (ExecutionMode mode : values()) {
            if (mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return LOCAL;
    }
}
