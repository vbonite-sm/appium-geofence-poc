package com.geofence.driver;

import io.appium.java_client.AppiumDriver;

/**
 * Interface for platform-specific driver providers.
 */
public interface DriverProvider {

    /**
     * Create and return an AppiumDriver instance.
     */
    AppiumDriver createDriver() throws Exception;

    /**
     * Check if this provider supports the given platform.
     */
    boolean supports(String platform);
}
