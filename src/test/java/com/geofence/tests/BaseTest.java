package com.geofence.tests;

import com.geofence.utils.BrowserStackDriver;
import com.geofence.utils.ConfigManager;
import com.geofence.utils.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.net.URI;
import java.net.URL;
import java.time.Duration;

public class BaseTest {

    protected AndroidDriver driver;
    protected String executionMode = "local";

    @Parameters({"executionMode"})
    @BeforeMethod
    public void setUp(@Optional("local") String executionMode) throws Exception {
        String systemMode = System.getProperty("executionMode");
        if (systemMode != null && !systemMode.isEmpty()) {
            executionMode = systemMode;
        }

        this.executionMode = executionMode;
        System.out.println("========================================");
        System.out.println("STARTING TEST SETUP");
        System.out.println("Execution Mode: " + executionMode.toUpperCase());
        System.out.println("========================================");

        if (executionMode.equalsIgnoreCase("browserstack")) {
            driver = BrowserStackDriver.createDriver();
        } else {
            driver = createLocalDriver();
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        DriverManager.setDriver(driver);

        System.out.println("APP LAUNCHED SUCCESSFULLY!");
    }

    private AndroidDriver createLocalDriver() throws Exception {
        UiAutomator2Options options = new UiAutomator2Options();

        options.setDeviceName(ConfigManager.get("local.device.name", "emulator-5554"));
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setCapability("autoGrantPermissions", true);

        // Use Geofence app
        String projectPath = System.getProperty("user.dir");
        String appPath = projectPath + "\\src\\test\\resources\\apps\\geofence-app.apk";
        options.setApp(appPath);

        System.out.println("App path: " + appPath);

        String appiumUrl = ConfigManager.get("appium.server.url", "http://127.0.0.1:4723");
        URL url = URI.create(appiumUrl).toURL();

        return new AndroidDriver(url, options);
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        System.out.println("========================================");
        System.out.println("CLEANING UP");
        System.out.println("========================================");

        if (executionMode.equalsIgnoreCase("browserstack") && driver != null) {
            reportToBrowserStack(result);
        }

        DriverManager.quitDriver();
        System.out.println("Driver quit successfully");
    }

    private void reportToBrowserStack(ITestResult result) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            if (result.isSuccess()) {
                js.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"passed\", \"reason\": \"Test passed\"}}");
                System.out.println("BrowserStack: Marked as PASSED");
            } else {
                String reason = result.getThrowable() != null
                        ? result.getThrowable().getMessage()
                        : "Test failed";
                reason = reason.replace("\"", "'").replace("\n", " ");
                if (reason.length() > 200) {
                    reason = reason.substring(0, 200) + "...";
                }
                js.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"failed\", \"reason\": \"" + reason + "\"}}");
                System.out.println("BrowserStack: Marked as FAILED");
            }
        } catch (Exception e) {
            System.err.println("Failed to report to BrowserStack: " + e.getMessage());
        }
    }
}