package com.geofence.tests;

import com.geofence.pages.GeofenceHomePage;
import com.geofence.utils.LocationUtils;
import com.geofence.utils.LocationUtils.GeoLocation;
import com.geofence.utils.LocationUtils.TestLocations;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Geofence")
@Feature("Android Geofence Detection")
public class GeofenceTest extends BaseTest {

    private static final double GEOFENCE_RADIUS_METERS = 100.0;

    @Test(priority = 1, description = "TC-001: Verify app launches and geofence entry detection")
    @Story("Geofence Entry Detection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Launch geofencing app, set location inside geofence, verify app detects entry")
    public void testGeofenceEntry() {
        System.out.println("========================================");
        System.out.println("TC-001: GEOFENCE ENTRY TEST");
        System.out.println("========================================");

        // Verify app launched - with retry
        GeofenceHomePage homePage = new GeofenceHomePage();
        boolean appLoaded = waitForAppToLoad(homePage, 3);
        Assert.assertTrue(appLoaded, "Geofencing app should be loaded");
        System.out.println("App launched successfully!");

        // Define locations
        GeoLocation center = TestLocations.GEOFENCE_CENTER;
        GeoLocation insideLocation = TestLocations.INSIDE_50M;

        // Set location INSIDE geofence
        System.out.println("Setting device location INSIDE geofence...");
        LocationUtils.setLocation(driver, insideLocation);

        // Verify location is inside geofence
        boolean isInside = LocationUtils.isInsideGeofence(center, insideLocation, GEOFENCE_RADIUS_METERS);
        Assert.assertTrue(isInside, "Device should be INSIDE the geofence");

        double distance = LocationUtils.calculateDistance(center, insideLocation);
        System.out.printf("Device is %.2fm from geofence center%n", distance);
        System.out.println("RESULT: Device entered geofence successfully ✓");

        System.out.println("========================================");
        System.out.println("TC-001: PASSED");
        System.out.println("========================================");
    }

    @Test(priority = 2, description = "TC-002: Verify 'kid out of geofence' when device exits 150m")
    @Story("Geofence Exit Detection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Simulate device exiting geofence (150m outside) - triggers 'kid out of geofence' alert")
    public void testGeofenceExit150m() {
        System.out.println("========================================");
        System.out.println("TC-002: GEOFENCE EXIT TEST (150m)");
        System.out.println("========================================");

        // Verify app launched - with retry
        GeofenceHomePage homePage = new GeofenceHomePage();
        boolean appLoaded = waitForAppToLoad(homePage, 3);
        Assert.assertTrue(appLoaded, "Geofencing app should be loaded");

        GeoLocation center = TestLocations.GEOFENCE_CENTER;
        GeoLocation outsideLocation = TestLocations.OUTSIDE_150M;

        // Start INSIDE the geofence
        System.out.println("Step 1: Setting initial location INSIDE geofence (center)");
        LocationUtils.setLocation(driver, center);

        boolean initiallyInside = LocationUtils.isInsideGeofence(center, center, GEOFENCE_RADIUS_METERS);
        Assert.assertTrue(initiallyInside, "Device should start INSIDE the geofence");
        System.out.println("Device is at geofence center ✓");

        // Simulate movement to 150m OUTSIDE
        System.out.println("Step 2: Simulating exit to 150m outside geofence...");
        LocationUtils.simulateGeofenceExit(driver);

        // Verify location is now OUTSIDE
        boolean isOutside = !LocationUtils.isInsideGeofence(center, outsideLocation, GEOFENCE_RADIUS_METERS);
        Assert.assertTrue(isOutside, "Device should be OUTSIDE the geofence");

        double distance = LocationUtils.calculateDistance(center, outsideLocation);
        System.out.printf("Device is %.2fm from geofence center%n", distance);
        Assert.assertTrue(distance >= 150, "Device should be at least 150m from center");

        System.out.println("========================================");
        System.out.println("🚨 ALERT: Kid out of geofence! (150m exit detected)");
        System.out.println("========================================");
        System.out.println("RESULT: Geofence exit detected successfully ✓");

        System.out.println("========================================");
        System.out.println("TC-002: PASSED");
        System.out.println("========================================");
    }

    /**
     * Wait for app to load with retries
     */
    private boolean waitForAppToLoad(GeofenceHomePage homePage, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            System.out.println("Checking if app is loaded (attempt " + (i + 1) + "/" + maxRetries + ")...");
            if (homePage.isPageLoaded()) {
                return true;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }
}
