package com.geofence.driver;

import com.geofence.config.EnvironmentConfig;
import com.geofence.models.Platform;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Driver provider for BrowserStack cloud execution.
 */
public class BrowserStackDriverProvider implements DriverProvider {

    private static final String HUB_URL = "https://%s:%s@hub-cloud.browserstack.com/wd/hub";

    private final EnvironmentConfig config;
    private final Platform platform;

    public BrowserStackDriverProvider(EnvironmentConfig config, Platform platform) {
        this.config = config;
        this.platform = platform;
    }

    @Override
    public AppiumDriver createDriver() throws Exception {
        validateCredentials();

        String hubUrl = String.format(HUB_URL,
                config.getBrowserStackUsername(),
                config.getBrowserStackAccessKey());

        return switch (platform) {
            case ANDROID -> createAndroidDriver(hubUrl);
            case IOS -> createIOSDriver(hubUrl);
        };
    }

    @Override
    public boolean supports(String platform) {
        return true;
    }

    private AndroidDriver createAndroidDriver(String hubUrl) throws Exception {
        UiAutomator2Options options = CapabilitiesBuilder.forAndroid()
                .withDeviceName(config.getBrowserStackDevice())
                .withPlatformVersion(config.getBrowserStackOsVersion())
                .withApp(config.getBrowserStackApp())
                .buildAndroidOptions();

        options.setCapability("bstack:options", buildBrowserStackOptions(false));
        return new AndroidDriver(new URL(hubUrl), options);
    }

    private IOSDriver createIOSDriver(String hubUrl) throws Exception {
        XCUITestOptions options = CapabilitiesBuilder.forIOS()
                .withDeviceName(config.getBrowserStackIOSDevice())
                .withPlatformVersion(config.getBrowserStackIOSVersion())
                .withApp(config.getBrowserStackIOSApp())
                .buildIOSOptions();

        options.setCapability("bstack:options", buildBrowserStackOptions(true));
        return new IOSDriver(new URL(hubUrl), options);
    }

    private Map<String, Object> buildBrowserStackOptions(boolean isIOS) {
        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("projectName", config.getBrowserStackProject());
        bstackOptions.put("buildName", isIOS ? config.getBrowserStackIOSBuild() : config.getBrowserStackBuild());
        bstackOptions.put("sessionName", isIOS ? config.getBrowserStackIOSName() : config.getBrowserStackName());
        bstackOptions.put("debug", true);
        bstackOptions.put("networkLogs", true);
        return bstackOptions;
    }

    private void validateCredentials() {
        String username = config.getBrowserStackUsername();
        String accessKey = config.getBrowserStackAccessKey();

        if (username == null || username.isEmpty() || accessKey == null || accessKey.isEmpty()) {
            throw new IllegalStateException(
                    "BrowserStack credentials not configured. Set BROWSERSTACK_USERNAME and BROWSERSTACK_ACCESSKEY environment variables.");
        }
    }
}
