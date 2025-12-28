package com.geofence.tests;

import com.geofence.pages.AccessibilityPage;
import com.geofence.pages.HomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for navigation using Page Object Model.
 * Notice how clean and readable the tests are!
 */
public class NavigationTest extends BaseTest {

    @Test(description = "Verify app launches and home page is displayed")
    public void testAppLaunches() {
        System.out.println("--- TEST: testAppLaunches ---");

        // Arrange & Act
        HomePage homePage = new HomePage();

        // Assert
        Assert.assertTrue(homePage.isPageLoaded(), "Home page should be loaded");
        Assert.assertTrue(homePage.isAccessibilityOptionVisible(), "Accessibility option should be visible");

        System.out.println("--- TEST PASSED ---");
    }

    @Test(description = "Verify navigation to Accessibility page")
    public void testNavigateToAccessibility() {
        System.out.println("--- TEST: testNavigateToAccessibility ---");

        // Arrange
        HomePage homePage = new HomePage();

        // Act
        AccessibilityPage accessibilityPage = homePage.clickAccessibilityOption();

        // Assert
        Assert.assertTrue(accessibilityPage.isPageLoaded(), "Accessibility page should be loaded");
        Assert.assertTrue(accessibilityPage.isAccessibilityNodeProviderOptionDisplayed(),
                "Accessibility Node Provider should be visible");

        System.out.println("--- TEST PASSED ---");
    }

    @Test(description = "Verify navigation back to home page")
    public void testNavigateBackToHome() {
        System.out.println("--- TEST: testNavigateBackToHome ---");

        // Arrange
        HomePage homePage = new HomePage();

        // Act - Navigate to Accessibility, then back
        AccessibilityPage accessibilityPage = homePage.clickAccessibilityOption();
        HomePage returnedHomePage = accessibilityPage.backToHomePage();

        // Assert
        Assert.assertTrue(returnedHomePage.isPageLoaded(), "Should be back on home page");
        Assert.assertTrue(returnedHomePage.isAnimationOptionVisible(),
                "Accessibility option should be visible again");

        System.out.println("--- TEST PASSED ---");
    }

    @Test(description = "Verify multiple navigation steps with fluent chaining")
    public void testMultipleNavigations() {
        System.out.println("--- TEST: testMultipleNavigations ---");

        // This test shows fluent chaining in action
        HomePage homePage = new HomePage();

        // Navigate: Home -> Accessibility -> Back to Home
        HomePage returnedHome = homePage
                .clickAccessibilityOption()
                .backToHomePage();

        // Verify we're back home
        Assert.assertTrue(returnedHome.isAnimationOptionVisible(),
                "Should see Accessibility after returning");

        System.out.println("--- TEST PASSED ---");
    }
}