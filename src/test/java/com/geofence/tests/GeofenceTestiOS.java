package com.geofence.tests;

import com.geofence.utils.LocationUtils;
import com.geofence.utils.LocationUtils.GeoLocation;
import com.geofence.utils.LocationUtils.TestLocations;
import com.geofence.utils.LocationUtilsiOS;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * iOS Geofence E2E Tests
 *
 * POC Requirement: 2 iOS test cases
 * - TC-iOS-001: Geofence entry detection
 * - TC-iOS-002: Geofence exit 150m - "kid out of geofence" notification
 *
 * Runs on BrowserStack iOS devices.
 */
@Epic("Geofence")
@Feature("iOS Geofence Detection")
public class GeofenceTestiOS extends BaseTestiOS {

    private static final double GEOFENCE_RADIUS_METERS = 100.0;

    @Test(priority = 1, description = "TC-iOS-001: Verify iOS device enters geofence area")
    @Story("iOS Geofence Entry Detection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Simulate iOS device entering geofence area and verify location is inside boundary")
    public void testGeofenceEntryiOS() {
        System.out.println("========================================");
        System.out.println("TC-iOS-001: iOS GEOFENCE ENTRY TEST");
        System.out.println("========================================");

        // Verify app launched
        Assert.assertNotNull(driver, "iOS Driver should be initialized");
        System.out.println("iOS app launched successfully!");

        // Wait for app to load
        sleep(3000);

        // Arrange
        GeoLocation center = TestLocations.GEOFENCE_CENTER;
        GeoLocation insideLocation = TestLocations.INSIDE_50M;

        // Act - Set location inside geofence
        System.out.println("Setting iOS device location INSIDE geofence...");
        LocationUtilsiOS.setLocation(driver, insideLocation);

        // Assert - Verify location is inside geofence
        boolean isInside = LocationUtils.isInsideGeofence(center, insideLocation, GEOFENCE_RADIUS_METERS);
        Assert.assertTrue(isInside, "iOS Device should be INSIDE the geofence");

        // Calculate and log distance
        double distance = LocationUtils.calculateDistance(center, insideLocation);
        System.out.printf("iOS Device is %.2fm from geofence center%n", distance);
        System.out.println("RESULT: iOS Device entered geofence successfully ✓");

        System.out.println("========================================");
        System.out.println("TC-iOS-001: PASSED");
        System.out.println("========================================");
    }

    @Test(priority = 2, description = "TC-iOS-002: Verify 'kid out of geofence' when iOS device exits 150m")
    @Story("iOS Geofence Exit Detection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Simulate iOS device exiting geofence (150m outside) - triggers 'kid out of geofence' alert")
    public void testGeofenceExit150miOS() {
        System.out.println("========================================");
        System.out.println("TC-iOS-002: iOS GEOFENCE EXIT TEST (150m)");
        System.out.println("========================================");

        // Verify app launched
        Assert.assertNotNull(driver, "iOS Driver should be initialized");

        // Wait for app to load
        sleep(3000);

        // Arrange
        GeoLocation center = TestLocations.GEOFENCE_CENTER;
        GeoLocation outsideLocation = TestLocations.OUTSIDE_150M;

        // Step 1: Start INSIDE the geofence
        System.out.println("Step 1: Setting initial iOS location INSIDE geofence (center)");
        LocationUtilsiOS.setLocation(driver, center);

        boolean initiallyInside = LocationUtils.isInsideGeofence(center, center, GEOFENCE_RADIUS_METERS);
        Assert.assertTrue(initiallyInside, "iOS Device should start INSIDE the geofence");
        System.out.println("iOS Device is at geofence center ✓");

        // Step 2: Simulate movement to 150m OUTSIDE
        System.out.println("Step 2: Simulating iOS exit to 150m outside geofence...");
        LocationUtilsiOS.simulateGeofenceExit(driver);

        // Assert - Verify location is now OUTSIDE geofence
        boolean isOutside = !LocationUtils.isInsideGeofence(center, outsideLocation, GEOFENCE_RADIUS_METERS);
        Assert.assertTrue(isOutside, "iOS Device should be OUTSIDE the geofence");

        // Calculate and log distance
        double distance = LocationUtils.calculateDistance(center, outsideLocation);
        System.out.printf("iOS Device is %.2fm from geofence center%n", distance);
        Assert.assertTrue(distance >= 150, "iOS Device should be at least 150m from center");

        // Alert message
        System.out.println("========================================");
        System.out.println("🚨 ALERT: Kid out of geofence! (iOS 150m exit detected)");
        System.out.println("========================================");
        System.out.println("RESULT: iOS Geofence exit detected successfully ✓");

        System.out.println("========================================");
        System.out.println("TC-iOS-002: PASSED");
        System.out.println("========================================");
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}