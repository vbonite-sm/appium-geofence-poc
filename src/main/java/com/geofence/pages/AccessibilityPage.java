package com.geofence.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

public class AccessibilityPage extends BasePage {

    // Elements //

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Accessibility Node Provider']")
    private WebElement accessibilityNodeProviderOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Accessibility Node Querying']")
    private WebElement accessibilityNodeQueryingOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Accessibility Service']")
    private WebElement accessibilityServiceOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Custom View']")
    private WebElement customViewOption;

    // Actions //

    public void clickAccessibilityNodeProviderOption() {
        System.out.println("Clicking on Accessibility Node Provider option");
        click(accessibilityNodeProviderOption);
    }

    public void clickAccessibilityServiceOption() {
        System.out.println("Clicking on Accessibility Service option");
        click(accessibilityServiceOption);
    }

    public HomePage backToHomePage() {
        System.out.println("Navigating back to Home Page");
        navigateBack();
        return new HomePage();
    }

    // Verifications //
    @Override
    public boolean isPageLoaded() {
        System.out.println("Verifying Accessibility Page is displayed");
        return isDisplayed(accessibilityNodeProviderOption);
    }

    public boolean isAccessibilityNodeProviderOptionDisplayed() {
        System.out.println("Verifying Accessibility Node Provider option is displayed");
        return isDisplayed(accessibilityNodeProviderOption);
    }

    public boolean isAccessibilityServiceOptionDisplayed() {
        System.out.println("Verifying Accessibility Service option is displayed");
        return isDisplayed(accessibilityServiceOption);
    }
}
