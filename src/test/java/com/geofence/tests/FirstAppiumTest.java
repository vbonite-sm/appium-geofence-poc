package com.geofence.tests;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URL;
import java.time.Duration;

public class FirstAppiumTest {

    private AndroidDriver driver;

    @BeforeMethod
    public void setUp() throws Exception {
        System.out.println("STARTING TEST SETUP");

        // Use UiAutomator2Options instead of DesiredCapabilities
        UiAutomator2Options options = new UiAutomator2Options();
        options.setDeviceName("emulator-5554");

        // App settings - Windows path
        String projectPath = System.getProperty("user.dir");
        String appPath = projectPath + "\\src\\test\\resources\\apps\\ApiDemos-debug.apk";
        options.setApp(appPath);

        // Print for debugging
        System.out.println("Project path: " + projectPath);
        System.out.println("App path: " + appPath);

        // Connect to Appium server
        URL appiumServerUrl = URI.create("http://127.0.0.1:4723").toURL();
        System.out.println("Connecting to Appium at: " + appiumServerUrl);

        // Create driver with options
        driver = new AndroidDriver(appiumServerUrl, options);

        // Set implicit wait
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        System.out.println("APP LAUNCHED SUCCESSFULLY!");
    }

    @Test
    public void testAppLaunches() {
        System.out.println("RUNNING TEST: testAppLaunches");

        Assert.assertNotNull(driver, "Driver should not be null");

        WebElement accessibilityOption = driver.findElement(
                By.xpath("//android.widget.TextView[@text='Accessibility']")
        );

        Assert.assertTrue(
                accessibilityOption.isDisplayed(),
                "Accessibility option should be visible on main screen"
        );

        System.out.println("Found 'Accessibility' on screen - app launched correctly!");
        System.out.println("TEST PASSED!");
    }

    @Test
    public void testClickAccessibility() {
        WebElement accessibilityOption = driver.findElement(
                By.xpath("//android.widget.TextView[@text='Accessibility']")
        );
        accessibilityOption.click();

        WebElement subMenuItem = driver.findElement(
                By.xpath("//android.widget.TextView[@text='Accessibility Node Provider']")
        );

        Assert.assertTrue(subMenuItem.isDisplayed());
    }

    @Test
    public void testNavigateBack() {
        driver.findElement(By.xpath("//android.widget.TextView[@text='Accessibility']")).click();
        driver.navigate().back();

        WebElement mainScreenElement = driver.findElement(
                By.xpath("//android.widget.TextView[@text='Accessibility']")
        );

        Assert.assertTrue(mainScreenElement.isDisplayed());
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
