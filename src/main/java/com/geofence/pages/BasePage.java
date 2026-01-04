package com.geofence.pages;

import com.geofence.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public abstract class BasePage {

    protected static final Logger log = LoggerFactory.getLogger(BasePage.class);
    protected static final int DEFAULT_TIMEOUT = 10;

    protected AppiumDriver driver;
    protected WebDriverWait wait;

    public BasePage() {
        this.driver = DriverManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(DEFAULT_TIMEOUT)), this);
    }

    protected void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
        element.click();
    }

    protected void type(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
        return element.getText();
    }

    protected boolean isDisplayed(WebElement element) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isDisplayed(WebElement element, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            customWait.until(ExpectedConditions.visibilityOf(element));
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void navigateBack() {
        driver.navigate().back();
    }

    public abstract boolean isPageLoaded();
}
