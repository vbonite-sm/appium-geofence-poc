package com.geofence.utils;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;

/**
 * Utility methods for Allure reporting.
 */
public class AllureUtils {

    private AllureUtils() {
    }

    public static void attachScreenshot(WebDriver driver, String name) {
        if (driver instanceof TakesScreenshot) {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshot), "png");
        }
    }

    @Attachment(value = "{name}", type = "text/plain")
    public static String attachText(String name, String content) {
        return content;
    }

    @Attachment(value = "{name}", type = "application/json")
    public static String attachJson(String name, String json) {
        return json;
    }

    public static void step(String stepName, Runnable action) {
        Allure.step(stepName, () -> {
            action.run();
        });
    }

    public static <T> T step(String stepName, java.util.function.Supplier<T> action) {
        return Allure.step(stepName, () -> action.get());
    }

    public static void addParameter(String name, String value) {
        Allure.parameter(name, value);
    }

    public static void addLink(String name, String url) {
        Allure.link(name, url);
    }
}
