package com.geofence.tests;

import com.geofence.pages.AccessibilityPage;
import com.geofence.pages.HomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for navigation using Page Object Model.
 */
public class NavigationTest extends BaseTest {

    @Test(description = "Verify app launches and home page is displayed")
    public void testAppLaunches() {
        log.info("Testing app launch and home page display");

        HomePage homePage = new HomePage();

        Assert.assertTrue(homePage.isPageLoaded(), "Home page should be loaded");
        Assert.assertTrue(homePage.isAccessibilityOptionVisible(), "Accessibility option should be visible");
    }

    @Test(description = "Verify navigation to Accessibility page")
    public void testNavigateToAccessibility() {
        log.info("Testing navigation to Accessibility page");

        HomePage homePage = new HomePage();
        AccessibilityPage accessibilityPage = homePage.clickAccessibilityOption();

        Assert.assertTrue(accessibilityPage.isPageLoaded(), "Accessibility page should be loaded");
        Assert.assertTrue(accessibilityPage.isAccessibilityNodeProviderOptionDisplayed(),
                "Accessibility Node Provider should be visible");
    }

    @Test(description = "Verify navigation back to home page")
    public void testNavigateBackToHome() {
        log.info("Testing navigation back to home page");

        HomePage homePage = new HomePage();
        AccessibilityPage accessibilityPage = homePage.clickAccessibilityOption();
        HomePage returnedHomePage = accessibilityPage.backToHomePage();

        Assert.assertTrue(returnedHomePage.isPageLoaded(), "Should be back on home page");
        Assert.assertTrue(returnedHomePage.isAnimationOptionVisible(),
                "Animation option should be visible again");
    }

    @Test(description = "Verify multiple navigation steps with fluent chaining")
    public void testMultipleNavigations() {
        log.info("Testing fluent navigation chaining");

        HomePage homePage = new HomePage();

        HomePage returnedHome = homePage
                .clickAccessibilityOption()
                .backToHomePage();

        Assert.assertTrue(returnedHome.isAnimationOptionVisible(),
                "Should see Animation option after returning");
    }
}