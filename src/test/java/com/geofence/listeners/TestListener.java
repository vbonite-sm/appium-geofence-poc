package com.geofence.listeners;

import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;

/**
 * Test listener for logging, screenshots, and Allure reporting.
 */
public class TestListener implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        log.info("Starting test: {}.{}", 
                result.getTestClass().getName(), 
                result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("Test PASSED: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("Test FAILED: {} - {}", 
                result.getMethod().getMethodName(),
                result.getThrowable().getMessage());

        captureScreenshot(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("Test SKIPPED: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onStart(ITestContext context) {
        log.info("Test suite started: {}", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();

        log.info("Test suite completed: {} - Passed: {}, Failed: {}, Skipped: {}",
                context.getName(), passed, failed, skipped);
    }

    private void captureScreenshot(ITestResult result) {
        try {
            WebDriver driver = getDriverFromTest(result);
            if (driver != null && driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("Screenshot on failure", 
                        "image/png", 
                        new ByteArrayInputStream(screenshot), 
                        "png");
                log.debug("Screenshot captured for failed test");
            }
        } catch (Exception e) {
            log.warn("Failed to capture screenshot: {}", e.getMessage());
        }
    }

    private WebDriver getDriverFromTest(ITestResult result) {
        Object testInstance = result.getInstance();
        try {
            Field driverField = findDriverField(testInstance.getClass());
            if (driverField != null) {
                driverField.setAccessible(true);
                return (WebDriver) driverField.get(testInstance);
            }
        } catch (Exception e) {
            log.debug("Could not retrieve driver from test instance: {}", e.getMessage());
        }
        return null;
    }

    private Field findDriverField(Class<?> clazz) {
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (WebDriver.class.isAssignableFrom(field.getType())) {
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}
