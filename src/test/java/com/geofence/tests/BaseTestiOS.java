package com.geofence.tests;

import com.geofence.utils.BrowserStackiOSDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/**
 * Base class for iOS tests.
 * Runs on BrowserStack only (no local iOS simulator on Windows).
 */
public class BaseTestiOS {

    protected IOSDriver driver;

    @BeforeMethod
    public void setUp() throws Exception {
        System.out.println("========================================");
        System.out.println("STARTING iOS TEST SETUP");
        System.out.println("Platform: iOS (BrowserStack)");
        System.out.println("========================================");

        driver = BrowserStackiOSDriver.createDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        System.out.println("========================================");
        System.out.println("iOS APP LAUNCHED SUCCESSFULLY!");
        System.out.println("========================================");
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        System.out.println("========================================");
        System.out.println("CLEANING UP iOS TEST");
        System.out.println("========================================");

        if (driver != null) {
            reportToBrowserStack(result);
            driver.quit();
        }

        System.out.println("iOS Driver quit successfully");
    }

    /**
     * Report test status to BrowserStack
     */
    private void reportToBrowserStack(ITestResult result) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            if (result.isSuccess()) {
                js.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"passed\", \"reason\": \"Test passed\"}}");
                System.out.println("BrowserStack: iOS Test Marked as PASSED");
            } else {
                String reason = result.getThrowable() != null
                        ? result.getThrowable().getMessage()
                        : "Test failed";
                reason = reason.replace("\"", "'").replace("\n", " ");
                if (reason.length() > 200) {
                    reason = reason.substring(0, 200) + "...";
                }
                js.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"failed\", \"reason\": \"" + reason + "\"}}");
                System.out.println("BrowserStack: iOS Test Marked as FAILED - " + reason);
            }
        } catch (Exception e) {
            System.err.println("Failed to report status to BrowserStack: " + e.getMessage());
        }
    }
}