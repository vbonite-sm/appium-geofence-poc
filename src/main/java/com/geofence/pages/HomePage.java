package com.geofence.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

public class HomePage extends BasePage {

    // Elements //
    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Accessibility']")
    private WebElement accessibilityOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Animation']")
    private WebElement animationOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='App']")
    private WebElement appOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Content']")
    private WebElement contentOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Graphics']")
    private WebElement graphicsOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Media']")
    private WebElement mediaOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='NFC']")
    private WebElement nfcOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='OS']")
    private WebElement osOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Preference']")
    private WebElement preferenceOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Text']")
    private WebElement textOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Views']")
    private WebElement viewsOption;

    // Actions //

    // Click on Accessibility option
    public AccessibilityPage clickAccessibilityOption() {
        System.out.println("Clicking on Accessibility option");
        click(accessibilityOption);
        return new AccessibilityPage();
    }

    // Click on Animation option
    public void clickAnimationOption() {
        System.out.println("Clicking on Animation option");
        click(animationOption);
    }

    // Click on App option
    public void clickAppOption() {
        System.out.println("Clicking on App option");
        click(appOption);
    }

    // Click on Preference option
    public void clickPreferenceOption() {
        System.out.println("Clicking on Preference option");
        click(preferenceOption);
    }

    // Click on Views option
    public void clickViewsOption() {
        System.out.println("Clicking on Views option");
        click(viewsOption);
    }

    // Verifications //

    // Check if homepage is loaded
    @Override
    public boolean isPageLoaded() {
        return isDisplayed(accessibilityOption);
    }

    public boolean isAccessibilityOptionVisible() {
        return isDisplayed(accessibilityOption);
    }

    public boolean isAnimationOptionVisible() {
        return isDisplayed(animationOption);
    }

    public boolean isViewsOptionVisible() {
        return isDisplayed(viewsOption);
    }
}