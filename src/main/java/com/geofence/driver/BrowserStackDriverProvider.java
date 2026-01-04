package com.geofence.driver;

import com.geofence.config.EnvironmentConfig;
import com.geofence.models.Platform;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.MutableCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Driver provider for BrowserStack cloud execution.
 */
public class BrowserStackDriverProvider implements DriverProvider {

    private static final Logger log = LoggerFactory.getLogger(BrowserStackDriverProvider.class);
    private static final String HUB_URL = "https://hub-cloud.browserstack.com/wd/hub";

    private final EnvironmentConfig config;
    private final Platform platform;

    public BrowserStackDriverProvider(EnvironmentConfig config, Platform platform) {
        this.config = config;
        this.platform = platform;
    }

    @Override
    public AppiumDriver createDriver() throws Exception {
        validateCredentials();

        return switch (platform) {
            case ANDROID -> createAndroidDriver();
            case IOS -> createIOSDriver();
        };
    }

    @Override
    public boolean supports(String platform) {
        return true;
    }

    private AndroidDriver createAndroidDriver() throws Exception {
        MutableCapabilities capabilities = new MutableCapabilities();

        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("userName", config.getBrowserStackUsername());
        bstackOptions.put("accessKey", config.getBrowserStackAccessKey());
        bstackOptions.put("deviceName", config.getBrowserStackDevice());
        bstackOptions.put("osVersion", config.getBrowserStackOsVersion());
        bstackOptions.put("projectName", config.getBrowserStackProject());
        bstackOptions.put("buildName", config.getBrowserStackBuild());
        bstackOptions.put("sessionName", config.getBrowserStackName());
        bstackOptions.put("debug", true);
        bstackOptions.put("networkLogs", true);
        bstackOptions.put("appiumVersion", "2.0.1");

        capabilities.setCapability("bstack:options", bstackOptions);
        capabilities.setCapability("platformName", "android");
        capabilities.setCapability("appium:app", config.getBrowserStackApp());
        capabilities.setCapability("appium:automationName", "UiAutomator2");

        log.info("Creating Android driver for device: {}", config.getBrowserStackDevice());
        return new AndroidDriver(new URL(HUB_URL), capabilities);
    }

    private IOSDriver createIOSDriver() throws Exception {
        MutableCapabilities capabilities = new MutableCapabilities();

        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("userName", config.getBrowserStackUsername());
        bstackOptions.put("accessKey", config.getBrowserStackAccessKey());
        bstackOptions.put("deviceName", config.getBrowserStackIOSDevice());
        bstackOptions.put("osVersion", config.getBrowserStackIOSVersion());
        bstackOptions.put("projectName", config.getBrowserStackProject());
        bstackOptions.put("buildName", config.getBrowserStackIOSBuild());
        bstackOptions.put("sessionName", config.getBrowserStackIOSName());
        bstackOptions.put("debug", true);
        bstackOptions.put("networkLogs", true);
        bstackOptions.put("appiumVersion", "2.0.1");

        capabilities.setCapability("bstack:options", bstackOptions);
        capabilities.setCapability("platformName", "ios");
        capabilities.setCapability("appium:app", config.getBrowserStackIOSApp());
        capabilities.setCapability("appium:automationName", "XCUITest");

        log.info("Creating iOS driver for device: {}", config.getBrowserStackIOSDevice());
        return new IOSDriver(new URL(HUB_URL), capabilities);
    }

    private void validateCredentials() {
        String username = config.getBrowserStackUsername();
        String accessKey = config.getBrowserStackAccessKey();

        log.debug("BrowserStack username present: {}", username != null && !username.isEmpty());
        log.debug("BrowserStack accessKey present: {}", accessKey != null && !accessKey.isEmpty());

        if (username == null || username.isEmpty() || accessKey == null || accessKey.isEmpty()) {
            throw new IllegalStateException(
                    "BrowserStack credentials not configured. Set BROWSERSTACK_USERNAME and BROWSERSTACK_ACCESSKEY environment variables, " +
                    "or pass via -Dbrowserstack.username and -Dbrowserstack.accesskey");
        }
    }
}
