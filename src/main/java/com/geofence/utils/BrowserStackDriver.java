package com.geofence.utils;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

// Utility class to create AndroidDriver for BrowserStack
public class BrowserStackDriver {

    private static final String BROWSERSTACK_URL = "https://%s:%s@hub-cloud.browserstack.com/wd/hub";

    // Create and return AndroidDriver connected to BrowserStack
    public static AndroidDriver createDriver() throws Exception {
        String username = ConfigManager.getBrowserStackUsername();
        String accessKey = ConfigManager.getBrowserStackAccessKey();

        if (username == null || accessKey == null) {
            throw new RuntimeException("BrowserStack credentials not found in config.properties");
        }

        String url = String.format(BROWSERSTACK_URL, username, accessKey);
        System.out.println("Connecting to BrowserStack...");

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
            throw new RuntimeException("BrowserStack app URL not configured. Upload your APK first.");
        }

        // Add BrowserStack options
        options.setCapability("bstack:options", bstackOptions);

        // Create driver
        AndroidDriver driver = new AndroidDriver(new URL(url), options);
        System.out.println("Connected to BrowserStack successfully!");

        return driver;
    }
}