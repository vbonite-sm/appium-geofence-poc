package com.geofence.tests;

import com.geofence.config.EnvironmentConfig;
import com.geofence.driver.DriverFactory;
import com.geofence.driver.DriverManager;
import com.geofence.listeners.TestListener;
import com.geofence.models.ExecutionMode;
import com.geofence.models.Platform;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Unified base test class using Template Method pattern.
 * Handles both Android and iOS tests with proper driver management.
 */
@Listeners(TestListener.class)
public abstract class BaseTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    protected AppiumDriver driver;
    protected ExecutionMode executionMode;
    protected Platform platform;
    protected EnvironmentConfig config;

    /**
     * Override to provide default platform for the test class.
     */
    protected Platform getDefaultPlatform() {
        return Platform.ANDROID;
    }

    /**
     * Override to provide default execution mode for the test class.
     */
    protected ExecutionMode getDefaultExecutionMode() {
        return ExecutionMode.LOCAL;
    }

    /**
     * Hook for subclasses to perform additional setup after driver initialization.
     */
    protected void onDriverInitialized() {
        // Subclasses can override
    }

    /**
     * Hook for subclasses to perform cleanup before driver quit.
     */
    protected void onBeforeDriverQuit(ITestResult result) {
        // Subclasses can override
    }

    @Parameters({"executionMode", "platform"})
    @BeforeMethod(alwaysRun = true)
    public final void setUp(
            @Optional String executionModeParam,
            @Optional String platformParam) throws Exception {

        this.config = EnvironmentConfig.getInstance();
        this.executionMode = resolveExecutionMode(executionModeParam);
        this.platform = resolvePlatform(platformParam);

        validateConfiguration();

        log.info("Test setup - Platform: {}, Mode: {}", platform, executionMode);

        driver = DriverFactory.createDriver(platform, executionMode, config);
        configureTimeouts();

        DriverManager.setDriver(driver);
        log.info("Driver initialized: {}", driver.getClass().getSimpleName());

        onDriverInitialized();
    }

    @AfterMethod(alwaysRun = true)
    public final void tearDown(ITestResult result) {
        try {
            onBeforeDriverQuit(result);

            if (!result.isSuccess() && driver != null) {
                captureFailureScreenshot(result);
            }

            if (executionMode.isCloud() && driver != null) {
                reportCloudStatus(result);
            }
        } finally {
            DriverManager.quitDriver();
            log.info("Test cleanup completed - Status: {}", result.isSuccess() ? "PASSED" : "FAILED");
        }
    }

    /**
     * Get the driver cast to AndroidDriver. Use when you need Android-specific methods.
     */
    protected AndroidDriver getAndroidDriver() {
        if (platform != Platform.ANDROID) {
            throw new IllegalStateException("Cannot get AndroidDriver for iOS test");
        }
        return (AndroidDriver) driver;
    }

    /**
     * Get the driver cast to IOSDriver. Use when you need iOS-specific methods.
     */
    protected IOSDriver getIOSDriver() {
        if (platform != Platform.IOS) {
            throw new IllegalStateException("Cannot get IOSDriver for Android test");
        }
        return (IOSDriver) driver;
    }

    protected void reportCloudStatus(ITestResult result) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String status = result.isSuccess() ? "passed" : "failed";
            String reason = result.isSuccess()
                    ? "Test passed"
                    : sanitizeMessage(result.getThrowable());

            String script = String.format(
                    "browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"%s\", \"reason\": \"%s\"}}",
                    status, reason);

            js.executeScript(script);
            log.debug("Reported {} status to cloud provider", status);

        } catch (Exception e) {
            log.warn("Could not report status to cloud provider: {}", e.getMessage());
        }
    }

    protected void captureFailureScreenshot(ITestResult result) {
        try {
            if (driver instanceof TakesScreenshot) {
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                String filename = String.format("%s_%s.png", result.getName(), timestamp);

                Path screenshotsDir = Paths.get("target", "screenshots");
                Files.createDirectories(screenshotsDir);

                Path destination = screenshotsDir.resolve(filename);
                Files.copy(screenshot.toPath(), destination);

                log.info("Screenshot saved: {}", destination);
            }
        } catch (Exception e) {
            log.warn("Failed to capture screenshot: {}", e.getMessage());
        }
    }

    private void configureTimeouts() {
        driver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(config.getImplicitWaitTimeout()));
    }

    private void validateConfiguration() {
        if (platform == Platform.IOS && executionMode == ExecutionMode.LOCAL) {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                log.warn("Local iOS testing not supported on Windows, switching to BrowserStack");
                this.executionMode = ExecutionMode.BROWSERSTACK;
            }
        }

        if (executionMode.isCloud()) {
            String username = config.getBrowserStackUsername();
            String accessKey = config.getBrowserStackAccessKey();
            if (username == null || username.isEmpty() || accessKey == null || accessKey.isEmpty()) {
                throw new IllegalStateException(
                        "Cloud credentials not configured. Set BROWSERSTACK_USERNAME and BROWSERSTACK_ACCESSKEY.");
            }
        }
    }

    private String sanitizeMessage(Throwable throwable) {
        if (throwable == null) {
            return "Test failed";
        }
        String message = throwable.getMessage();
        if (message == null) {
            return "Test failed with " + throwable.getClass().getSimpleName();
        }
        message = message.replace("\"", "'").replace("\n", " ");
        return message.length() > 200 ? message.substring(0, 200) + "..." : message;
    }

    private ExecutionMode resolveExecutionMode(String param) {
        String systemMode = System.getProperty("executionMode");
        if (systemMode != null && !systemMode.isEmpty()) {
            return ExecutionMode.fromString(systemMode);
        }
        if (param != null && !param.isEmpty()) {
            return ExecutionMode.fromString(param);
        }
        return getDefaultExecutionMode();
    }

    private Platform resolvePlatform(String param) {
        String systemPlatform = System.getProperty("platform");
        if (systemPlatform != null && !systemPlatform.isEmpty()) {
            return Platform.fromString(systemPlatform);
        }
        if (param != null && !param.isEmpty()) {
            return Platform.fromString(param);
        }
        return getDefaultPlatform();
    }

    protected void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected boolean isAndroid() {
        return platform == Platform.ANDROID;
    }

    protected boolean isIOS() {
        return platform == Platform.IOS;
    }
}