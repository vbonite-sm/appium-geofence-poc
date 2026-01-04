package com.geofence.tests;

import com.geofence.models.ExecutionMode;
import com.geofence.models.Platform;
import io.appium.java_client.ios.IOSDriver;

/**
 * Base class for iOS-specific tests.
 * Extends BaseTest with iOS defaults and convenience methods.
 */
public abstract class BaseTestiOS extends BaseTest {

    @Override
    protected Platform getDefaultPlatform() {
        return Platform.IOS;
    }

    @Override
    protected ExecutionMode getDefaultExecutionMode() {
        return ExecutionMode.BROWSERSTACK;
    }

    /**
     * Convenience method to get the iOS driver directly.
     */
    protected IOSDriver ios() {
        return getIOSDriver();
    }

    @Override
    protected void onDriverInitialized() {
        log.debug("iOS driver ready: {}", driver.getCapabilities().getBrowserName());
    }
}