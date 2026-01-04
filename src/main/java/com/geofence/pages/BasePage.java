package com.geofence.pages;

import com.geofence.utils.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {

    protected AndroidDriver driver;
    protected WebDriverWait wait;

    private static final int DEFAULT_TIMEOUT = 10;

    // Constructor for driver and page factory  initialization
    public BasePage() {
        this.driver = DriverManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(DEFAULT_TIMEOUT)), this);
    }

    // COMMON FUNCTIONS FOR ALL PAGES

    // Click on element
    protected void click(WebElement e) {
        wait.until(ExpectedConditions.elementToBeClickable(e));
        e.click();
    }

    // Input text into element
    protected void type(WebElement e, String text) {
        wait.until(ExpectedConditions.visibilityOf(e));
        e.clear();
        e.sendKeys(text);
    }

    // Get text from element
    protected String getText(WebElement e) {
        wait.until(ExpectedConditions.visibilityOf(e));
        return e.getText();
    }

    // Check if element is displayed
    protected boolean isDisplayed(WebElement e) {
        try {
            wait.until(ExpectedConditions.visibilityOf(e));
            return e.isDisplayed();
        } catch (Exception ex) {
            return false;
        }
    }

    // Wait for element to be visible
    protected WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void navigateBack() {
        driver.navigate().back();
    }

    public abstract boolean isPageLoaded();
}
