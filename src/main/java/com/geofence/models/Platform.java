package com.geofence.models;

/**
 * Supported mobile platforms.
 */
public enum Platform {
    ANDROID("Android", "UiAutomator2"),
    IOS("iOS", "XCUITest");

    private final String platformName;
    private final String automationName;

    Platform(String platformName, String automationName) {
        this.platformName = platformName;
        this.automationName = automationName;
    }

    public String getPlatformName() {
        return platformName;
    }

    public String getAutomationName() {
        return automationName;
    }

    public static Platform fromString(String value) {
        if (value == null || value.isBlank()) {
            return ANDROID;
        }
        for (Platform p : values()) {
            if (p.name().equalsIgnoreCase(value) || p.platformName.equalsIgnoreCase(value)) {
                return p;
            }
        }
        return ANDROID;
    }
}
