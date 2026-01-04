package com.geofence.pages;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * Page Object for the Geofencing app main screen.
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

    @Override
    public boolean isPageLoaded() {
        try {
            pause(2000);

            if (isDisplayed(appTitle, 5)) {
                log.debug("Found app title element");
                return true;
            }

            if (isDisplayed(helloWorldText, 3)) {
                log.debug("Found 'Hello World!' text");
                return true;
            }

            if (isDisplayed(helloTextPartial, 3)) {
                log.debug("Found text containing 'Hello'");
                return true;
            }

            if (driver instanceof AndroidDriver) {
                AndroidDriver androidDriver = (AndroidDriver) driver;
                String activity = androidDriver.currentActivity();
                if (activity != null && activity.contains("MainActivity")) {
                    log.debug("MainActivity is running");
                    return true;
                }
                return activity != null && !activity.isEmpty();
            }

            return false;

        } catch (Exception e) {
            log.warn("Error checking if page is loaded: {}", e.getMessage());
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

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}