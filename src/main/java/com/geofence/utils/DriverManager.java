package com.geofence.utils;

import io.appium.java_client.android.AndroidDriver;

/**
 * Utility class to manage the AndroidDriver instance.
 */

public class DriverManager {

    // Threadlocal to hold AndroidDriver instances for thread safety (parallel tests
    private static final ThreadLocal<AndroidDriver> driverThreadLocal = new ThreadLocal<>();

    private DriverManager() {
        // Private constructor to prevent instantiation
    }

    // Getter for theAndroidDriver instance
    public static AndroidDriver getDriver() {
        return driverThreadLocal.get();
    }

    // Setter for the AndroidDriver instance
    public static void setDriver(AndroidDriver driver) {
        driverThreadLocal.set(driver);
    }

    // Quit and remove the AndroidDriver instance
    public static void quitDriver() {
        AndroidDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
        }
    }
}
