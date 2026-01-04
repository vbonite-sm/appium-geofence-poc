package com.geofence.services;

import com.geofence.driver.DriverManager;
import com.geofence.models.GeoLocation;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.html5.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for handling device location operations.
 */
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);
    private static final int DEFAULT_MOVEMENT_STEPS = 5;
    private static final int DEFAULT_STEP_DELAY_MS = 1000;

    private final AppiumDriver driver;

    public LocationService() {
        this.driver = DriverManager.getDriver();
    }

    public LocationService(AppiumDriver driver) {
        this.driver = driver;
    }

    /**
     * Set the device location to the specified coordinates.
     */
    public void setLocation(GeoLocation location) {
        log.debug("Setting device location to: {}", location);

        Location seleniumLocation = new Location(
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude()
        );

        if (driver instanceof AndroidDriver androidDriver) {
            androidDriver.setLocation(seleniumLocation);
        } else if (driver instanceof IOSDriver iosDriver) {
            iosDriver.setLocation(seleniumLocation);
        } else {
            throw new UnsupportedOperationException("setLocation not supported for this driver type");
        }
        pause(500);

        log.info("Location set to: {}", location);
    }

    /**
     * Simulate gradual movement between two locations.
     */
    public void simulateMovement(GeoLocation from, GeoLocation to) {
        simulateMovement(from, to, DEFAULT_MOVEMENT_STEPS, DEFAULT_STEP_DELAY_MS);
    }

    /**
     * Simulate gradual movement with custom parameters.
     */
    public void simulateMovement(GeoLocation from, GeoLocation to, int steps, int delayMs) {
        log.info("Simulating movement from {} to {} in {} steps", from, to, steps);

        double latStep = (to.getLatitude() - from.getLatitude()) / steps;
        double lonStep = (to.getLongitude() - from.getLongitude()) / steps;

        for (int i = 0; i <= steps; i++) {
            double currentLat = from.getLatitude() + (latStep * i);
            double currentLon = from.getLongitude() + (lonStep * i);

            GeoLocation intermediate = GeoLocation.builder()
                    .latitude(currentLat)
                    .longitude(currentLon)
                    .name("Step " + i)
                    .build();

            setLocation(intermediate);
            pause(delayMs);
        }

        log.info("Movement simulation completed");
    }

    /**
     * Check if a location is within a geofence.
     */
    public boolean isInsideGeofence(GeoLocation center, GeoLocation point, double radiusMeters) {
        return point.isWithinRadius(center, radiusMeters);
    }

    /**
     * Calculate distance between two locations.
     */
    public double calculateDistance(GeoLocation from, GeoLocation to) {
        return from.distanceTo(to);
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
