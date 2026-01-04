package com.geofence.driver;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

/**
 * Thread-safe driver manager for parallel test execution.
 */
public class DriverManager {

    private static final ThreadLocal<AppiumDriver> driverThreadLocal = new ThreadLocal<>();

    private DriverManager() {
    }

    public static AppiumDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static AndroidDriver getAndroidDriver() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver instanceof AndroidDriver) {
            return (AndroidDriver) driver;
        }
        throw new IllegalStateException("Current driver is not an AndroidDriver");
    }

    public static IOSDriver getIOSDriver() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver instanceof IOSDriver) {
            return (IOSDriver) driver;
        }
        throw new IllegalStateException("Current driver is not an IOSDriver");
    }

    public static void setDriver(AppiumDriver driver) {
        driverThreadLocal.set(driver);
    }

    public static void quitDriver() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    public static boolean hasDriver() {
        return driverThreadLocal.get() != null;
    }
}
