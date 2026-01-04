package com.geofence.driver;

import com.geofence.models.Platform;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Fluent builder for Appium capabilities.
 * Supports both Android (UiAutomator2) and iOS (XCUITest) options.
 */
public class CapabilitiesBuilder {

    private String deviceName;
    private String platformVersion;
    private String app;
    private String automationName;
    private String udid;
    private String bundleId;
    private String appPackage;
    private String appActivity;
    private Duration newCommandTimeout;
    private boolean autoGrantPermissions;
    private boolean noReset;
    private boolean fullReset;
    private final Platform platform;
    private final Map<String, Object> additionalCapabilities = new HashMap<>();

    private CapabilitiesBuilder(Platform platform) {
        this.platform = platform;
        this.automationName = platform.getAutomationName();
        this.newCommandTimeout = Duration.ofSeconds(300);
    }

    public static CapabilitiesBuilder forAndroid() {
        return new CapabilitiesBuilder(Platform.ANDROID);
    }

    public static CapabilitiesBuilder forIOS() {
        return new CapabilitiesBuilder(Platform.IOS);
    }

    public static CapabilitiesBuilder forPlatform(Platform platform) {
        return new CapabilitiesBuilder(platform);
    }

    public CapabilitiesBuilder withDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    public CapabilitiesBuilder withPlatformVersion(String version) {
        this.platformVersion = version;
        return this;
    }

    public CapabilitiesBuilder withApp(String appPath) {
        this.app = appPath;
        return this;
    }

    public CapabilitiesBuilder withAutomationName(String automationName) {
        this.automationName = automationName;
        return this;
    }

    public CapabilitiesBuilder withUdid(String udid) {
        this.udid = udid;
        return this;
    }

    public CapabilitiesBuilder withBundleId(String bundleId) {
        this.bundleId = bundleId;
        return this;
    }

    public CapabilitiesBuilder withAppPackage(String appPackage) {
        this.appPackage = appPackage;
        return this;
    }

    public CapabilitiesBuilder withAppActivity(String appActivity) {
        this.appActivity = appActivity;
        return this;
    }

    public CapabilitiesBuilder withNewCommandTimeout(Duration timeout) {
        this.newCommandTimeout = timeout;
        return this;
    }

    public CapabilitiesBuilder withAutoGrantPermissions(boolean autoGrant) {
        this.autoGrantPermissions = autoGrant;
        return this;
    }

    public CapabilitiesBuilder withNoReset(boolean noReset) {
        this.noReset = noReset;
        return this;
    }

    public CapabilitiesBuilder withFullReset(boolean fullReset) {
        this.fullReset = fullReset;
        return this;
    }

    public CapabilitiesBuilder withCapability(String key, Object value) {
        this.additionalCapabilities.put(key, value);
        return this;
    }

    public UiAutomator2Options buildAndroidOptions() {
        if (platform != Platform.ANDROID) {
            throw new IllegalStateException("Cannot build Android options for iOS platform");
        }

        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName(platform.getPlatformName());
        options.setAutomationName(automationName);
        options.setNewCommandTimeout(newCommandTimeout);

        if (deviceName != null) {
            options.setDeviceName(deviceName);
        }
        if (platformVersion != null) {
            options.setPlatformVersion(platformVersion);
        }
        if (app != null && !app.isEmpty()) {
            options.setApp(app);
        }
        if (udid != null) {
            options.setUdid(udid);
        }
        if (appPackage != null) {
            options.setAppPackage(appPackage);
        }
        if (appActivity != null) {
            options.setAppActivity(appActivity);
        }
        if (autoGrantPermissions) {
            options.setCapability("autoGrantPermissions", true);
        }
        if (noReset) {
            options.setNoReset(true);
        }
        if (fullReset) {
            options.setFullReset(true);
        }

        additionalCapabilities.forEach(options::setCapability);
        return options;
    }

    public XCUITestOptions buildIOSOptions() {
        if (platform != Platform.IOS) {
            throw new IllegalStateException("Cannot build iOS options for Android platform");
        }

        XCUITestOptions options = new XCUITestOptions();
        options.setPlatformName(platform.getPlatformName());
        options.setAutomationName(automationName);
        options.setNewCommandTimeout(newCommandTimeout);

        if (deviceName != null) {
            options.setDeviceName(deviceName);
        }
        if (platformVersion != null) {
            options.setPlatformVersion(platformVersion);
        }
        if (app != null && !app.isEmpty()) {
            options.setApp(app);
        }
        if (udid != null) {
            options.setUdid(udid);
        }
        if (bundleId != null) {
            options.setBundleId(bundleId);
        }
        if (noReset) {
            options.setNoReset(true);
        }
        if (fullReset) {
            options.setFullReset(true);
        }

        additionalCapabilities.forEach(options::setCapability);
        return options;
    }

    public Platform getPlatform() {
        return platform;
    }
}
