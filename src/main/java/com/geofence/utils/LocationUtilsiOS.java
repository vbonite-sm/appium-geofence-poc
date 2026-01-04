package com.geofence.utils;

import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.html5.Location;

/**
 * Location utilities for iOS testing.
 */
public class LocationUtilsiOS {

    /**
     * Set device location on iOS
     */
    public static void setLocation(IOSDriver driver, double latitude, double longitude, double altitude) {
        System.out.printf("Setting iOS location to: (%.6f, %.6f)%n", latitude, longitude);

        try {
            Location location = new Location(latitude, longitude, altitude);
            driver.setLocation(location);
            System.out.println("iOS Location set successfully!");
        } catch (Exception e) {
            System.err.println("Warning: Could not set iOS location - " + e.getMessage());
        }

        sleep(2000);
    }

    /**
     * Set location using predefined test locations
     */
    public static void setLocation(IOSDriver driver, LocationUtils.GeoLocation geoLocation) {
        setLocation(driver, geoLocation.getLatitude(), geoLocation.getLongitude(), geoLocation.getAltitude());
    }

    /**
     * Simulate geofence entry on iOS
     */
    public static void simulateGeofenceEntry(IOSDriver driver) {
        System.out.println("=== iOS: SIMULATING GEOFENCE ENTRY ===");

        LocationUtils.GeoLocation start = LocationUtils.TestLocations.OUTSIDE_150M;
        LocationUtils.GeoLocation end = LocationUtils.TestLocations.GEOFENCE_CENTER;

        simulateMovement(driver, start, end, 3, 2000);

        System.out.println("=== iOS: GEOFENCE ENTRY COMPLETE ===");
    }

    /**
     * Simulate geofence exit on iOS (150m)
     */
    public static void simulateGeofenceExit(IOSDriver driver) {
        System.out.println("=== iOS: SIMULATING GEOFENCE EXIT (150m) ===");

        LocationUtils.GeoLocation start = LocationUtils.TestLocations.GEOFENCE_CENTER;
        LocationUtils.GeoLocation end = LocationUtils.TestLocations.OUTSIDE_150M;

        simulateMovement(driver, start, end, 3, 2000);

        System.out.println("=== iOS: GEOFENCE EXIT COMPLETE ===");
    }

    /**
     * Simulate movement between two locations
     */
    public static void simulateMovement(IOSDriver driver, LocationUtils.GeoLocation from,
                                        LocationUtils.GeoLocation to, int steps, int delayMs) {
        System.out.println("iOS: Simulating movement from " + from + " to " + to);

        double latStep = (to.getLatitude() - from.getLatitude()) / steps;
        double lonStep = (to.getLongitude() - from.getLongitude()) / steps;

        for (int i = 0; i <= steps; i++) {
            double lat = from.getLatitude() + (latStep * i);
            double lon = from.getLongitude() + (lonStep * i);

            setLocation(driver, lat, lon, 0);
            sleep(delayMs);
        }

        System.out.println("iOS: Movement simulation complete!");
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}