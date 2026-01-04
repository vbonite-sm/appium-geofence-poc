package com.geofence.utils;

import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates IOSDriver for BrowserStack cloud execution.
 */
public class BrowserStackiOSDriver {
    
    private static final Logger logger = LoggerFactory.getLogger(BrowserStackiOSDriver.class);
    private static final String BROWSERSTACK_URL = "https://%s:%s@hub-cloud.browserstack.com/wd/hub";
    
    private BrowserStackiOSDriver() {
        // Private constructor to hide implicit public one
    }

    /**
     * Create driver for BrowserStack iOS
     * @return IOSDriver instance
     * @throws BrowserStackDriverException if driver creation fails
     */
    public static IOSDriver createDriver() throws BrowserStackDriverException {
        String username = ConfigManager.getBrowserStackUsername();
        String accessKey = ConfigManager.getBrowserStackAccessKey();

        if (username == null || accessKey == null) {
            throw new BrowserStackDriverException("BrowserStack credentials not found in config.properties");
        }

        String url = String.format(BROWSERSTACK_URL, username, accessKey);
        logger.info("Connecting to BrowserStack for iOS...");

        // Build capabilities
        XCUITestOptions options = new XCUITestOptions();

        // BrowserStack specific options
        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("projectName", ConfigManager.get("browserstack.project", "Geofence POC"));
        bstackOptions.put("buildName", ConfigManager.get("browserstack.build.ios", "iOS Build 1.0"));
        bstackOptions.put("sessionName", ConfigManager.get("browserstack.name.ios", "iOS Geofence Test"));
        bstackOptions.put("debug", true);
        bstackOptions.put("networkLogs", true);

        // Device settings
        options.setPlatformName("iOS");
        options.setDeviceName(ConfigManager.get("browserstack.ios.device", "iPhone 14"));
        options.setPlatformVersion(ConfigManager.get("browserstack.ios.version", "16"));
        options.setAutomationName("XCUITest");

        // App - use BrowserStack app URL
        String appUrl = ConfigManager.get("browserstack.ios.app");
        if (appUrl != null && !appUrl.isEmpty() && !appUrl.contains("YOUR_APP_ID")) {
            options.setApp(appUrl);
        } else {
            // Use BrowserStack sample app for demo
            options.setApp("bs://sample.app");
            logger.info("Using BrowserStack sample iOS app");
        }

        // Auto accept alerts (for location permissions)
        options.setCapability("autoAcceptAlerts", true);

        // Add BrowserStack options
        options.setCapability("bstack:options", bstackOptions);

        try {
            // Create driver using URI instead of deprecated URL constructor
            IOSDriver driver = new IOSDriver(URI.create(url).toURL(), options);
            logger.info("Connected to BrowserStack iOS successfully!");
            return driver;
        } catch (Exception e) {
            throw new BrowserStackDriverException("Failed to create iOS driver", e);
        }
    }
    
    /**
     * Custom exception for BrowserStack driver errors
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