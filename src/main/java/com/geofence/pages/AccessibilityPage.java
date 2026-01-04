package com.geofence.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * Page Object for the Accessibility screen.
 */
public class AccessibilityPage extends BasePage {

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Accessibility Node Provider']")
    private WebElement accessibilityNodeProviderOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Accessibility Node Querying']")
    private WebElement accessibilityNodeQueryingOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Accessibility Service']")
    private WebElement accessibilityServiceOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Custom View']")
    private WebElement customViewOption;

    public void clickAccessibilityNodeProviderOption() {
        log.debug("Clicking on Accessibility Node Provider option");
        click(accessibilityNodeProviderOption);
    }

    public void clickAccessibilityServiceOption() {
        log.debug("Clicking on Accessibility Service option");
        click(accessibilityServiceOption);
    }

    public HomePage backToHomePage() {
        log.debug("Navigating back to Home Page");
        navigateBack();
        return new HomePage();
    }

    @Override
    public boolean isPageLoaded() {
        return isDisplayed(accessibilityNodeProviderOption);
    }

    public boolean isAccessibilityNodeProviderOptionDisplayed() {
        return isDisplayed(accessibilityNodeProviderOption);
    }

    public boolean isAccessibilityServiceOptionDisplayed() {
        return isDisplayed(accessibilityServiceOption);
    }
}
