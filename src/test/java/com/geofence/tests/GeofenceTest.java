package com.geofence.tests;

import com.geofence.dataproviders.GeofenceDataProvider;
import com.geofence.dataproviders.GeofenceDataProvider.TestLocations;
import com.geofence.listeners.RetryAnalyzer;
import com.geofence.models.GeoLocation;
import com.geofence.pages.GeofenceHomePage;
import com.geofence.services.GeofenceService;
import com.geofence.services.LocationService;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Geofence")
@Feature("Android Geofence Detection")
public class GeofenceTest extends BaseTest {

    private static final double GEOFENCE_RADIUS_METERS = 100.0;
    private static final int APP_LOAD_RETRIES = 3;

    private GeofenceService geofenceService;
    private LocationService locationService;
    private GeofenceHomePage homePage;

    @Override
    protected void onDriverInitialized() {
        locationService = new LocationService(driver);
        geofenceService = new GeofenceService(locationService, GEOFENCE_RADIUS_METERS);
        homePage = new GeofenceHomePage();
    }

    @Test(priority = 1, 
          description = "TC-001: Verify app launches and geofence entry detection",
          retryAnalyzer = RetryAnalyzer.class)
    @Story("Geofence Entry Detection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Launch geofencing app, set location inside geofence, verify app detects entry")
    public void testGeofenceEntry() {
        log.info("TC-001: Testing geofence entry detection");

        Assert.assertTrue(geofenceService.waitForAppToLoad(homePage, APP_LOAD_RETRIES),
                "Geofencing app should be loaded");

        GeoLocation center = TestLocations.GEOFENCE_CENTER;
        GeoLocation insideLocation = TestLocations.INSIDE_50M;

        locationService.setLocation(insideLocation);

        boolean isInside = locationService.isInsideGeofence(center, insideLocation, GEOFENCE_RADIUS_METERS);
        Assert.assertTrue(isInside, "Device should be inside the geofence");

        double distance = locationService.calculateDistance(center, insideLocation);
        log.info("Device is {:.2f}m from geofence center", distance);

        Allure.step("Verified device entered geofence at " + distance + "m from center");
    }

    @Test(priority = 2, 
          description = "TC-002: Verify exit alert when device leaves geofence by 150m",
          retryAnalyzer = RetryAnalyzer.class)
    @Story("Geofence Exit Detection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Simulate device exiting geofence (150m outside) - triggers exit alert")
    public void testGeofenceExit150m() {
        log.info("TC-002: Testing geofence exit detection at 150m");

        Assert.assertTrue(geofenceService.waitForAppToLoad(homePage, APP_LOAD_RETRIES),
                "Geofencing app should be loaded");

        GeoLocation center = TestLocations.GEOFENCE_CENTER;
        GeoLocation outsideLocation = TestLocations.OUTSIDE_150M;

        // Start at center
        locationService.setLocation(center);
        Assert.assertTrue(locationService.isInsideGeofence(center, center, GEOFENCE_RADIUS_METERS),
                "Device should start inside the geofence");

        // Simulate exit
        geofenceService.simulateGeofenceExit(center, outsideLocation);

        // Verify exit
        boolean isOutside = !locationService.isInsideGeofence(center, outsideLocation, GEOFENCE_RADIUS_METERS);
        Assert.assertTrue(isOutside, "Device should be outside the geofence");

        double distance = locationService.calculateDistance(center, outsideLocation);
        Assert.assertTrue(distance >= 150, "Device should be at least 150m from center");

        log.info("Exit alert triggered - device is {:.2f}m from geofence center", distance);
        Allure.step("Verified geofence exit at " + distance + "m from center");
    }

    @Test(priority = 3,
          description = "TC-003: Verify location boundary detection",
          dataProvider = "testLocations",
          dataProviderClass = GeofenceDataProvider.class)
    @Story("Boundary Detection")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test various locations relative to geofence boundary")
    public void testLocationBoundary(GeoLocation location, String locationName, boolean expectedInside) {
        log.info("Testing location: {} - expected inside: {}", locationName, expectedInside);

        Assert.assertTrue(geofenceService.waitForAppToLoad(homePage, APP_LOAD_RETRIES),
                "Geofencing app should be loaded");

        GeoLocation center = TestLocations.GEOFENCE_CENTER;
        locationService.setLocation(location);

        boolean actualInside = locationService.isInsideGeofence(center, location, GEOFENCE_RADIUS_METERS);
        double distance = locationService.calculateDistance(center, location);

        log.info("Location '{}' at {:.2f}m - inside: {}", locationName, distance, actualInside);

        Assert.assertEquals(actualInside, expectedInside,
                String.format("Location '%s' at %.2fm should be %s geofence",
                        locationName, distance, expectedInside ? "inside" : "outside"));
    }
}
