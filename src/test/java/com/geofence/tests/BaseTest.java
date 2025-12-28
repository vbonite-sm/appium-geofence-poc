package com.geofence.tests;

import com.geofence.utils.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.net.URI;
import java.net.URL;
import java.time.Duration;

 // Base class for all tests.

public class BaseTest {

    protected AndroidDriver driver;

    // Configuration - can be moved to config file later
    private static final String APPIUM_SERVER_URL = "http://127.0.0.1:4723";
    private static final String DEVICE_NAME = "emulator-5554";
    private static final String APP_PATH = "\\src\\test\\resources\\apps\\ApiDemos-debug.apk";

    @BeforeMethod
    public void setUp() throws Exception {
        System.out.println("STARTING TEST SETUP");

        // Build capabilities using UiAutomator2Options (modern API)
        UiAutomator2Options options = new UiAutomator2Options();

        // Device settings
        options.setDeviceName(DEVICE_NAME);
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");

        // App path
        String projectPath = System.getProperty("user.dir");
        String fullAppPath = projectPath + APP_PATH;
        options.setApp(fullAppPath);

        System.out.println("App path: " + fullAppPath);

        // Create driver
        URL appiumServerUrl = URI.create(APPIUM_SERVER_URL).toURL();
        driver = new AndroidDriver(appiumServerUrl, options);

        // Set implicit wait
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // Store driver in DriverManager so pages can access it
        DriverManager.setDriver(driver);

        System.out.println("APP LAUNCHED SUCCESSFULLY!");
    }

    @AfterMethod
    public void tearDown() {
        System.out.println("CLEANING UP");

        DriverManager.quitDriver();

        System.out.println("Driver quit successfully");
    }
}