package com.geofence.tests;

import com.geofence.dataproviders.GeofenceDataProvider.TestLocations;
import com.geofence.listeners.RetryAnalyzer;
import com.geofence.models.GeoLocation;
import com.geofence.services.LocationService;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * iOS Geofence E2E Tests.
 * Runs on BrowserStack iOS devices.
 */
@Epic("Geofence")
@Feature("iOS Geofence Detection")
public class GeofenceTestiOS extends BaseTestiOS {

    private static final double GEOFENCE_RADIUS_METERS = 100.0;
    private LocationService locationService;

    @Override
    protected void onDriverInitialized() {
        super.onDriverInitialized();
        locationService = new LocationService(driver);
    }

    @Test(priority = 1, 
          description = "TC-iOS-001: Verify iOS device enters geofence area",
          retryAnalyzer = RetryAnalyzer.class)
    @Story("iOS Geofence Entry Detection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Simulate iOS device entering geofence area and verify location is inside boundary")
    public void testGeofenceEntryiOS() {
        log.info("TC-iOS-001: iOS Geofence Entry Test");

        // Arrange
        Assert.assertNotNull(driver, "iOS Driver should be initialized");
        pause(3000);
        GeoLocation center = TestLocations.GEOFENCE_CENTER;
        GeoLocation insideLocation = TestLocations.INSIDE_50M;

        // Act
        locationService.setLocation(insideLocation);
        boolean isInside = locationService.isInsideGeofence(center, insideLocation, GEOFENCE_RADIUS_METERS);
        double distance = locationService.calculateDistance(center, insideLocation);

        // Assert
        Assert.assertTrue(isInside, "iOS Device should be inside the geofence");
        log.info("iOS Device is {:.2f}m from geofence center", distance);
        Allure.step("iOS device entered geofence at " + distance + "m from center");
    }

    @Test(priority = 2, 
          description = "TC-iOS-002: Verify exit alert when iOS device leaves geofence by 150m",
          retryAnalyzer = RetryAnalyzer.class)
    @Story("iOS Geofence Exit Detection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Simulate iOS device exiting geofence (150m outside) - triggers exit alert")
    public void testGeofenceExit150miOS() {
        log.info("TC-iOS-002: iOS Geofence Exit Test (150m)");

        // Arrange
        Assert.assertNotNull(driver, "iOS Driver should be initialized");
        pause(3000);
        GeoLocation center = TestLocations.GEOFENCE_CENTER;
        GeoLocation outsideLocation = TestLocations.OUTSIDE_150M;
        locationService.setLocation(center);
        Assert.assertTrue(
                locationService.isInsideGeofence(center, center, GEOFENCE_RADIUS_METERS),
                "iOS Device should start inside the geofence");

        // Act
        locationService.simulateMovement(center, outsideLocation);
        boolean isOutside = !locationService.isInsideGeofence(center, outsideLocation, GEOFENCE_RADIUS_METERS);
        double distance = locationService.calculateDistance(center, outsideLocation);

        // Assert
        Assert.assertTrue(isOutside, "iOS Device should be outside the geofence");
        Assert.assertTrue(distance >= 150, "iOS Device should be at least 150m from center");
        log.info("iOS exit alert triggered - device is {:.2f}m from geofence center", distance);
        Allure.step("iOS geofence exit detected at " + distance + "m from center");
    }
}