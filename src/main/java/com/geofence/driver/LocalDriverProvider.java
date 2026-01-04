package com.geofence.driver;

import com.geofence.config.EnvironmentConfig;
import com.geofence.models.Platform;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.URI;
import java.net.URL;

/**
 * Driver provider for local Appium server execution.
 */
public class LocalDriverProvider implements DriverProvider {

    private final EnvironmentConfig config;
    private final Platform platform;

    public LocalDriverProvider(EnvironmentConfig config, Platform platform) {
        this.config = config;
        this.platform = platform;
    }

    @Override
    public AndroidDriver createDriver() throws Exception {
        if (platform != Platform.ANDROID) {
            throw new UnsupportedOperationException("Local iOS testing not supported on Windows");
        }

        UiAutomator2Options options = CapabilitiesBuilder.forAndroid()
                .withDeviceName(config.getLocalDeviceName())
                .withApp(resolveAppPath())
                .withAutoGrantPermissions(true)
                .buildAndroidOptions();

        URL appiumUrl = URI.create(config.getAppiumServerUrl()).toURL();
        return new AndroidDriver(appiumUrl, options);
    }

    @Override
    public boolean supports(String platform) {
        return Platform.ANDROID.name().equalsIgnoreCase(platform);
    }

    private String resolveAppPath() {
        String appPath = config.getLocalAppPath();
        if (appPath.startsWith("\\") || appPath.startsWith("/")) {
            return System.getProperty("user.dir") + appPath;
        }
        return appPath;
    }
}
