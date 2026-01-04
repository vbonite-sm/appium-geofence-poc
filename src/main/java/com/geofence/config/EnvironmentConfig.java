package com.geofence.config;

import org.yaml.snakeyaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Environment-aware configuration manager with support for YAML and properties files.
 * Supports configuration override via system properties and environment variables.
 */
public class EnvironmentConfig {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentConfig.class);
    private static final String CONFIG_DIR = "config/";
    private static final String DEFAULT_ENV = "local";

    private static volatile EnvironmentConfig instance;

    private final Map<String, Object> config;
    private final String activeEnvironment;

    private EnvironmentConfig() {
        this.activeEnvironment = resolveEnvironment();
        this.config = loadConfiguration();
        log.info("Configuration loaded for environment: {}", activeEnvironment);
    }

    public static EnvironmentConfig getInstance() {
        if (instance == null) {
            synchronized (EnvironmentConfig.class) {
                if (instance == null) {
                    instance = new EnvironmentConfig();
                }
            }
        }
        return instance;
    }

    /**
     * Get a configuration value by key with type inference.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        // Check system property first
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return convertValue(systemValue, defaultValue);
        }

        // Check environment variable
        String envKey = key.toUpperCase().replace(".", "_");
        String envValue = System.getenv(envKey);
        if (envValue != null) {
            return convertValue(envValue, defaultValue);
        }

        // Get from loaded config
        Object value = getNestedValue(key);
        if (value != null) {
            return (T) value;
        }

        return defaultValue;
    }

    public String get(String key) {
        return get(key, (String) null);
    }

    @SuppressWarnings("unchecked")
    private <T> T convertValue(String value, T defaultValue) {
        if (defaultValue == null) {
            return (T) value;
        }
        if (defaultValue instanceof Integer) {
            return (T) Integer.valueOf(value);
        }
        if (defaultValue instanceof Double) {
            return (T) Double.valueOf(value);
        }
        if (defaultValue instanceof Boolean) {
            return (T) Boolean.valueOf(value);
        }
        return (T) value;
    }

    private Object getNestedValue(String key) {
        String[] parts = key.split("\\.");
        Object current = config;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }

    private String resolveEnvironment() {
        String env = System.getProperty("env");
        if (env == null) {
            env = System.getenv("TEST_ENV");
        }
        return env != null ? env : DEFAULT_ENV;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadConfiguration() {
        Map<String, Object> mergedConfig = new HashMap<>();

        // Load base configuration
        mergedConfig.putAll(loadYamlConfig("application.yaml"));

        // Load environment-specific configuration (overrides base)
        String envConfigFile = "application-" + activeEnvironment + ".yaml";
        mergedConfig.putAll(loadYamlConfig(envConfigFile));

        // Fallback to properties file if YAML not found
        if (mergedConfig.isEmpty()) {
            mergedConfig.putAll(loadPropertiesConfig("config.properties"));
        }

        return mergedConfig;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadYamlConfig(String filename) {
        String path = CONFIG_DIR + filename;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is != null) {
                Yaml yaml = new Yaml();
                Map<String, Object> loaded = yaml.load(is);
                log.debug("Loaded configuration from {}", path);
                return loaded != null ? loaded : new HashMap<>();
            }
        } catch (Exception e) {
            log.debug("Could not load {}: {}", path, e.getMessage());
        }
        return new HashMap<>();
    }

    private Map<String, Object> loadPropertiesConfig(String filename) {
        Map<String, Object> propMap = new HashMap<>();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                props.forEach((k, v) -> propMap.put(k.toString(), v));
                log.debug("Loaded configuration from {}", filename);
            }
        } catch (Exception e) {
            log.debug("Could not load {}: {}", filename, e.getMessage());
        }
        return propMap;
    }

    // Convenience methods for common configuration values

    public String getActiveEnvironment() {
        return activeEnvironment;
    }

    public String getBrowserStackUsername() {
        return get("browserstack.username");
    }

    public String getBrowserStackAccessKey() {
        return get("browserstack.accesskey");
    }

    public String getBrowserStackApp() {
        return get("browserstack.app");
    }

    public String getBrowserStackDevice() {
        return get("browserstack.device", "Google Pixel 7");
    }

    public String getBrowserStackOsVersion() {
        return get("browserstack.os_version", "13.0");
    }

    public String getBrowserStackProject() {
        return get("browserstack.project", "GeoFence POC");
    }

    public String getBrowserStackBuild() {
        return get("browserstack.build", "Build 1.0");
    }

    public String getBrowserStackName() {
        return get("browserstack.name", "Geofence Tests");
    }

    public String getBrowserStackIOSApp() {
        return get("browserstack.ios.app");
    }

    public String getBrowserStackIOSDevice() {
        return get("browserstack.ios.device", "iPhone 14");
    }

    public String getBrowserStackIOSVersion() {
        return get("browserstack.ios.version", "16");
    }

    public String getBrowserStackIOSBuild() {
        return get("browserstack.build.ios", "iOS Build 1.0");
    }

    public String getBrowserStackIOSName() {
        return get("browserstack.name.ios", "iOS Geofence Tests");
    }

    public String getAppiumServerUrl() {
        return get("appium.server.url", "http://127.0.0.1:4723");
    }

    public String getLocalDeviceName() {
        return get("local.device.name", "emulator-5554");
    }

    public String getLocalAppPath() {
        return get("local.app.path", "\\src\\test\\resources\\apps\\geofence-app.apk");
    }

    public String getApiBaseUri() {
        return get("api.base.uri", "https://jsonplaceholder.typicode.com");
    }

    public int getDefaultTimeout() {
        return get("timeout.default", 10);
    }

    public int getImplicitWaitTimeout() {
        return get("timeout.implicit", 10);
    }
}
