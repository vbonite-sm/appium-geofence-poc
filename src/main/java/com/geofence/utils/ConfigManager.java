package com.geofence.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

// Utility class to manage configuration properties
public class ConfigManager {

    private static Properties properties;
    private static final String CONFIG_PATH = "src/test/resources/config.properties";

    static {
        loadProperties();
    }

    private static void loadProperties() {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            properties.load(fis);
            System.out.println("Configuration loaded successfully");
        } catch (IOException e) {
            System.err.println("Warning: Could not load config.properties - " + e.getMessage());
        }
    }

    // Get property by key
    public static String get(String key) {
        // First check system property (allows override from command line)
        String value = System.getProperty(key);
        if (value != null) {
            return value;
        }
        // Then check environment variable
        value = System.getenv(key.toUpperCase().replace(".", "_"));
        if (value != null) {
            return value;
        }
        // Finally check properties file
        return properties.getProperty(key);
    }

    // Get property with default value
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    // Get BrowserStack username
    public static String getBrowserStackUsername() {
        return get("browserstack.username");
    }

    // Get BrowserStack access key
    public static String getBrowserStackAccessKey() {
        return get("browserstack.accesskey");
    }

    // Get BrowserStack app identifier
    public static String getBrowserStackApp() {
        return get("browserstack.app");
    }
}