package com.geofence.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * Page Object for a sample home page (API Demos style app).
 */
public class HomePage extends BasePage {

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

    public AccessibilityPage clickAccessibilityOption() {
        log.debug("Clicking on Accessibility option");
        click(accessibilityOption);
        return new AccessibilityPage();
    }

    public void clickAnimationOption() {
        log.debug("Clicking on Animation option");
        click(animationOption);
    }

    public void clickAppOption() {
        log.debug("Clicking on App option");
        click(appOption);
    }

    public void clickPreferenceOption() {
        log.debug("Clicking on Preference option");
        click(preferenceOption);
    }

    public void clickViewsOption() {
        log.debug("Clicking on Views option");
        click(viewsOption);
    }

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