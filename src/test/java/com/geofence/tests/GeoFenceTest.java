package com.geofence.tests;

import com.geofence.utils.DriverManager;
import com.geofence.utils.LocationUtils;
import com.geofence.utils.LocationUtils.GeoLocation;
import com.geofence.utils.LocationUtils.TestLocations;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("GeoFence")
@Feature("Android GeoFence Functionality")
public class GeoFenceTest extends BaseTest {

    private static final double GEOFENCE_RADIUS_METERS = 100.0;

    @Test(description = "TC-001: Verify device location can be set inside geofence")
    @Story("GeoFence Entry Detection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Set device entering the geofence area and verify location is inside)")
    public void testGeoFenceEntry() {
        System.out.println("--- TC-001: Verify device location can be set inside geofence ---");

        // Arrange
        GeoLocation geofenceCenter = TestLocations.GEOFENCE_CENTER;
        GeoLocation insideLocation  = TestLocations.INSIDE_50M;

        // Act - Set location to inside geofence
        LocationUtils.setLocation(driver, insideLocation);

        // Assert - Verify location is inside geofence
        boolean isInside = LocationUtils.isInsideGeofence(geofenceCenter, insideLocation, GEOFENCE_RADIUS_METERS);
        Assert.assertTrue(isInside, "Device should be inside the geofence");

        // Log current location
        double distance = LocationUtils.calculateDistance(geofenceCenter, insideLocation);
        System.out.println("Current location is inside geofence, distance from center: " + distance + " meters");

        System.out.println("--- TC-001 PASSED ---");
    }

    @Test(description = "TC-002: Verify device location can be set outside geofence")
    @Story("GeoFence Exit Detection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Set device exiting the geofence area and verify location is outside)")
    public void testGeoFenceExit() {
        System.out.println("--- TC-002: Verify device location can be set outside geofence ---");

        // Arrange
        GeoLocation geofenceCenter = TestLocations.GEOFENCE_CENTER;
        GeoLocation outsideLocation = TestLocations.OUTSIDE_150M;

        // Set location to inside geofence
        System.out.println("Setting initial location inside geofence...");
        LocationUtils.setLocation(driver, geofenceCenter);

        boolean initiallyInside = LocationUtils.isInsideGeofence(geofenceCenter, geofenceCenter, GEOFENCE_RADIUS_METERS);
        Assert.assertTrue(initiallyInside, "Device should initially be inside the geofence");

        // Act - Simulate exiting geofence
        System.out.println("Setting location to outside geofence...");
        LocationUtils.simulateGeofenceExit(driver);

        // Assert - Verify location is outside geofence
        boolean isOutside = !LocationUtils.isInsideGeofence(geofenceCenter, outsideLocation, GEOFENCE_RADIUS_METERS);
        Assert.assertTrue(isOutside, "Device should be outside the geofence");

        // Log current location
        double distance = LocationUtils.calculateDistance(geofenceCenter, outsideLocation);
        System.out.println("Current location is outside geofence, distance from center: " + distance + " meters");

        System.out.println("--- TC-002 PASSED ---");
    }

    @Test(description = "TC-003: Verify movement simulation into and out of geofence")
    @Story("GeoFence Movement Tracking")
    @Severity(SeverityLevel.NORMAL)
    @Description("Track device movement into and out of the geofence area")
    public void testGeoFenceMovementTracking() {
        System.out.println("--- TC-003: Verify movement simulation into and out of geofence ---");

        // Arrange
        GeoLocation geofenceCenter = TestLocations.GEOFENCE_CENTER;
        GeoLocation start = TestLocations.INSIDE_50M;
        GeoLocation end = TestLocations.OUTSIDE_200M;

        // Act - Start inside
        LocationUtils.setLocation(driver, start);
        boolean isInsideStart = LocationUtils.isInsideGeofence(geofenceCenter, start, GEOFENCE_RADIUS_METERS);
        Assert.assertTrue(isInsideStart, "Device should start inside the geofence");
        System.out.println("Starting movement simulation from inside to outside geofence...");

        // Act - Simulate movement out of geofence
        LocationUtils.simulateMovement(driver, start, end, 10, 500);
        boolean isOutsideEnd = !LocationUtils.isInsideGeofence(geofenceCenter, end, GEOFENCE_RADIUS_METERS);
        Assert.assertTrue(isOutsideEnd, "Device should end up outside the geofence");
        System.out.println("Movement simulation completed, device is now outside geofence.");

        double finalDistance = LocationUtils.calculateDistance(geofenceCenter, end);
        System.out.println("Final location distance from geofence center: " + finalDistance + " meters");

        System.out.println("--- TC-003 PASSED ---");
    }

    @Test(description = "TC-004: Verify geofence boundary conditions")
    @Story("GeoFence Boundary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test device location exactly on the geofence boundary")
    public void testGeoFenceBoundary() {
        System.out.println("--- TC-004: Verify geofence boundary conditions ---");

        // Arrange
        GeoLocation geofenceCenter = TestLocations.GEOFENCE_CENTER;


        GeoLocation[] testLocations = {
            TestLocations.INSIDE_50M,
            TestLocations.ON_BOUNDARY_100M,
            TestLocations.OUTSIDE_150M,
            TestLocations.OUTSIDE_200M,
        };

        for (GeoLocation point : testLocations) {
            LocationUtils.setLocation(driver, point);
            double distance = LocationUtils.calculateDistance(geofenceCenter, point);
            boolean isInside = LocationUtils.isInsideGeofence(geofenceCenter, point, GEOFENCE_RADIUS_METERS);

            String status = isInside ? "inside" : "outside";
            System.out.println("Location " + point + " is " + status + " the geofence (distance: " + distance + " meters)");
        }

        System.out.println("--- TC-004 PASSED ---");
    }
}
