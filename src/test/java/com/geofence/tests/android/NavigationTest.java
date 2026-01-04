package com.geofence.tests.android;

import com.geofence.pages.AccessibilityPage;
import com.geofence.pages.HomePage;
import com.geofence.tests.base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Android tests for navigation using Page Object Model.
 */
public class NavigationTest extends BaseTest {

    @Test(description = "Verify app launches and home page is displayed")
    public void testAppLaunches() {
        log.info("Testing app launch and home page display");

        // Arrange
        HomePage homePage = new HomePage();

        // Act & Assert
        Assert.assertTrue(homePage.isPageLoaded(), "Home page should be loaded");
        Assert.assertTrue(homePage.isAccessibilityOptionVisible(), "Accessibility option should be visible");
    }

    @Test(description = "Verify navigation to Accessibility page")
    public void testNavigateToAccessibility() {
        log.info("Testing navigation to Accessibility page");

        // Arrange
        HomePage homePage = new HomePage();

        // Act
        AccessibilityPage accessibilityPage = homePage.clickAccessibilityOption();

        // Assert
        Assert.assertTrue(accessibilityPage.isPageLoaded(), "Accessibility page should be loaded");
        Assert.assertTrue(accessibilityPage.isAccessibilityNodeProviderOptionDisplayed(),
                "Accessibility Node Provider should be visible");
    }

    @Test(description = "Verify navigation back to home page")
    public void testNavigateBackToHome() {
        log.info("Testing navigation back to home page");

        // Arrange
        HomePage homePage = new HomePage();
        AccessibilityPage accessibilityPage = homePage.clickAccessibilityOption();

        // Act
        HomePage returnedHomePage = accessibilityPage.backToHomePage();

        // Assert
        Assert.assertTrue(returnedHomePage.isPageLoaded(), "Should be back on home page");
        Assert.assertTrue(returnedHomePage.isAnimationOptionVisible(),
                "Animation option should be visible again");
    }

    @Test(description = "Verify multiple navigation steps with fluent chaining")
    public void testMultipleNavigations() {
        log.info("Testing fluent navigation chaining");

        // Arrange
        HomePage homePage = new HomePage();

        // Act
        HomePage returnedHome = homePage
                .clickAccessibilityOption()
                .backToHomePage();

        // Assert
        Assert.assertTrue(returnedHome.isAnimationOptionVisible(),
                "Should see Animation option after returning");
    }
}
