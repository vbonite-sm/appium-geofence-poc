package com.geofence.utils;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to create AndroidDriver for BrowserStack.
 */
public class BrowserStackDriver {
    
    private static final Logger logger = LoggerFactory.getLogger(BrowserStackDriver.class);
    private static final String BROWSERSTACK_URL = "https://%s:%s@hub-cloud.browserstack.com/wd/hub";
    
    private BrowserStackDriver() {
        // Private constructor to hide implicit public one
    }

    /**
     * Create and return AndroidDriver connected to BrowserStack.
     * @return AndroidDriver instance
     * @throws BrowserStackDriverException if driver creation fails
     */
    public static AndroidDriver createDriver() throws BrowserStackDriverException {
        String username = ConfigManager.getBrowserStackUsername();
        String accessKey = ConfigManager.getBrowserStackAccessKey();

        if (username == null || accessKey == null) {
            throw new BrowserStackDriverException("BrowserStack credentials not found in config.properties");
        }

        String url = String.format(BROWSERSTACK_URL, username, accessKey);
        logger.info("Connecting to BrowserStack...");

        // Build capabilities
        UiAutomator2Options options = new UiAutomator2Options();

        // BrowserStack specific options
        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("projectName", ConfigManager.get("browserstack.project", "Geofence POC"));
        bstackOptions.put("buildName", ConfigManager.get("browserstack.build", "Build 1.0"));
        bstackOptions.put("sessionName", ConfigManager.get("browserstack.name", "Geofence Test"));
        bstackOptions.put("debug", true);
        bstackOptions.put("networkLogs", true);

        // Device settings
        options.setPlatformName("Android");
        options.setDeviceName(ConfigManager.get("browserstack.device", "Google Pixel 7"));
        options.setPlatformVersion(ConfigManager.get("browserstack.os_version", "13.0"));
        options.setAutomationName("UiAutomator2");

        // App - use BrowserStack app URL
        String appUrl = ConfigManager.getBrowserStackApp();
        if (appUrl != null && !appUrl.isEmpty() && !appUrl.contains("YOUR_APP_ID")) {
            options.setApp(appUrl);
        } else {
            throw new BrowserStackDriverException("BrowserStack app URL not configured. Upload your APK first.");
        }

        // Add BrowserStack options
        options.setCapability("bstack:options", bstackOptions);

        try {
            // Create driver using URI instead of deprecated URL constructor
            AndroidDriver driver = new AndroidDriver(URI.create(url).toURL(), options);
            logger.info("Connected to BrowserStack successfully!");
            return driver;
        } catch (Exception e) {
            throw new BrowserStackDriverException("Failed to create Android driver", e);
        }
    }
    
    /**
     * Custom exception for BrowserStack driver errors.
     */
    public static class BrowserStackDriverException extends Exception {
        public BrowserStackDriverException(String message) {
            super(message);
        }
        
        public BrowserStackDriverException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}