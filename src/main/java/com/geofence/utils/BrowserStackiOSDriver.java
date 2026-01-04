package com.geofence.utils;

import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates IOSDriver for BrowserStack cloud execution.
 */
public class BrowserStackiOSDriver {

    private static final String BROWSERSTACK_URL = "https://%s:%s@hub-cloud.browserstack.com/wd/hub";

    /**
     * Create driver for BrowserStack iOS
     */
    public static IOSDriver createDriver() throws Exception {
        String username = ConfigManager.getBrowserStackUsername();
        String accessKey = ConfigManager.getBrowserStackAccessKey();

        if (username == null || accessKey == null) {
            throw new RuntimeException("BrowserStack credentials not found in config.properties");
        }

        String url = String.format(BROWSERSTACK_URL, username, accessKey);
        System.out.println("Connecting to BrowserStack for iOS...");

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
            System.out.println("Using BrowserStack sample iOS app");
        }

        // Auto accept alerts (for location permissions)
        options.setCapability("autoAcceptAlerts", true);

        // Add BrowserStack options
        options.setCapability("bstack:options", bstackOptions);

        // Create driver
        IOSDriver driver = new IOSDriver(new URL(url), options);
        System.out.println("Connected to BrowserStack iOS successfully!");

        return driver;
    }
}