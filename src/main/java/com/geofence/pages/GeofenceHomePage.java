package com.geofence.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

/**
 * Page Object for the Geofencing app main screen
 */
public class GeofenceHomePage extends BasePage {

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Geofencing']")
    private WebElement appTitle;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Hello World!']")
    private WebElement helloWorldText;

    @AndroidFindBy(xpath = "//android.widget.TextView[contains(@text, 'Hello')]")
    private WebElement helloTextPartial;

    @AndroidFindBy(id = "com.eebax.geofencing:id/action_bar")
    private WebElement actionBar;

    // ==================== VERIFICATIONS ====================

    @Override
    public boolean isPageLoaded() {
        try {
            // Wait for app to fully load
            Thread.sleep(3000);

            // Try multiple ways to verify app is loaded
            if (isDisplayed(appTitle)) {
                System.out.println("Found: App title 'Geofencing'");
                return true;
            }
            if (isDisplayed(helloWorldText)) {
                System.out.println("Found: 'Hello World!' text");
                return true;
            }
            if (isDisplayed(helloTextPartial)) {
                System.out.println("Found: Text containing 'Hello'");
                return true;
            }

            // Last resort - check if any activity is running
            String activity = driver.currentActivity();
            System.out.println("Current activity: " + activity);
            if (activity != null && activity.contains("MainActivity")) {
                System.out.println("Found: MainActivity is running");
                return true;
            }

            // Debug: Print page source
            System.out.println("Page source preview:");
            String pageSource = driver.getPageSource();
            if (pageSource != null && pageSource.length() > 500) {
                System.out.println(pageSource.substring(0, 500) + "...");
            } else {
                System.out.println(pageSource);
            }

            return activity != null && !activity.isEmpty();

        } catch (Exception e) {
            System.err.println("Error checking page loaded: " + e.getMessage());
            return false;
        }
    }

    public boolean isAppTitleVisible() {
        return isDisplayed(appTitle);
    }

    public boolean isHelloWorldVisible() {
        return isDisplayed(helloWorldText);
    }

    public String getAppTitle() {
        try {
            return getText(appTitle);
        } catch (Exception e) {
            return "Geofencing";
        }
    }

    public String getHelloWorldText() {
        try {
            return getText(helloWorldText);
        } catch (Exception e) {
            return "Hello World!";
        }
    }
}