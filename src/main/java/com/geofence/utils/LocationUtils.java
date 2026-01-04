package com.geofence.utils;

import io.appium.java_client.android.AndroidDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(LocationUtils.class);
    
    private LocationUtils() {
        // Private constructor to hide implicit public one
    }

    public static class GeoLocation {
        private final double latitude;
        private final double longitude;
        private final double altitude;
        private final String name;

        public GeoLocation(double latitude, double longitude, double altitude, String name) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = altitude;
            this.name = name;
        }

        public GeoLocation(double latitude, double longitude, String name) {
            this(latitude, longitude, 0.0, name);
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public double getAltitude() {
            return altitude;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return String.format("%s (%.6f, %.6f)", name, latitude, longitude);
        }
    }

    public static class TestLocations {
        
        private TestLocations() {
            // Private constructor to hide implicit public one
        }
        
        // San Francisco - Geofence Center
        public static final GeoLocation GEOFENCE_CENTER =
                new GeoLocation(37.7749, -122.4194, "Geofence Center (SF)");

        // 50m from center - INSIDE geofence (100m radius)
        public static final GeoLocation INSIDE_50M =
                new GeoLocation(37.7753, -122.4194, "Inside 50m");

        // 100m from center - ON BOUNDARY
        public static final GeoLocation ON_BOUNDARY_100M =
                new GeoLocation(37.7758, -122.4194, "On Boundary 100m");

        // 150m from center - OUTSIDE geofence (triggers exit)
        public static final GeoLocation OUTSIDE_150M =
                new GeoLocation(37.7763, -122.4194, "Outside 150m");

        // 200m from center - FAR OUTSIDE
        public static final GeoLocation OUTSIDE_200M =
                new GeoLocation(37.7767, -122.4194, "Outside 200m");

        // New York (completely different location)
        public static final GeoLocation NEW_YORK =
                new GeoLocation(40.7128, -74.0060, "New York");
    }

    // Set device location with Appium
    public static void setLocation(AndroidDriver driver, GeoLocation location) {
        logger.info("Setting device location to: {}", location);

        org.openqa.selenium.html5.Location seleniumLocation =
                new org.openqa.selenium.html5.Location(
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAltitude()
                );

        driver.setLocation(seleniumLocation);

        sleep(1000); // Wait for location to take effect

        logger.info("Location successfully set to: {}", location);
    }

    // Simulate movement between two locations
    public static void simulateMovement(AndroidDriver driver, GeoLocation start, GeoLocation end, int steps, int delayMs) {
        logger.info("Simulating movement from {} to {}", start, end);

        double latStep = (end.getLatitude() - start.getLatitude()) / steps;
        double lonStep = (end.getLongitude() - start.getLongitude()) / steps;

        for (int i = 0; i <= steps; i++) {
            double currentLat = start.getLatitude() + (latStep * i);
            double currentLon = start.getLongitude() + (lonStep * i);
            GeoLocation intermediateLocation = new GeoLocation(currentLat, currentLon, "Step " + i);
            setLocation(driver, intermediateLocation);
            sleep(delayMs);
        }

        logger.info("Movement simulation completed.");
    }

    // Simulate entering geofence
    public static void simulateGeofenceEntry(AndroidDriver driver) {
        logger.info("Simulating geofence entry...");
        simulateMovement(
                driver,
                TestLocations.OUTSIDE_150M,
                TestLocations.GEOFENCE_CENTER,
                5,
                1000
        );
        logger.info("Geofence entry simulation completed.");
    }

    // Simulate exiting geofence
    public static void simulateGeofenceExit(AndroidDriver driver) {
        logger.info("Simulating geofence exit...");
        simulateMovement(
                driver,
                TestLocations.GEOFENCE_CENTER,
                TestLocations.OUTSIDE_150M,
                5,
                1000
        );
        logger.info("Geofence exit simulation completed.");
    }

    // Calculate distance between two locations
    public static double calculateDistance(GeoLocation loc1, GeoLocation loc2) {
        final double EARTH_RADIUS_METERS = 6371000.0; // in meters

        double lat1Rad = Math.toRadians(loc1.getLatitude());
        double lat2Rad = Math.toRadians(loc2.getLatitude());
        double deltaLatRad = Math.toRadians(loc2.getLatitude() - loc1.getLatitude());
        double deltaLonRad = Math.toRadians(loc2.getLongitude() - loc1.getLongitude());

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    // Verify if location is inside geofence
    public static boolean isInsideGeofence(GeoLocation location, GeoLocation center, double radiusMeters) {
        double distance = calculateDistance(location, center);
        boolean inside = distance <= radiusMeters;
        String status = inside ? "inside" : "outside";
        logger.info("{} is {} the geofence (distance: {} meters)", location, status, distance);
        return inside;
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
