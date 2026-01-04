package com.geofence.services;

import com.geofence.models.GeoLocation;
import com.geofence.pages.GeofenceHomePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service layer for geofence-related business operations.
 * Abstracts complex geofence workflows from test classes.
 */
public class GeofenceService {

    private static final Logger log = LoggerFactory.getLogger(GeofenceService.class);
    private static final double DEFAULT_GEOFENCE_RADIUS = 100.0;

    private final LocationService locationService;
    private final double geofenceRadius;

    public GeofenceService() {
        this(new LocationService(), DEFAULT_GEOFENCE_RADIUS);
    }

    public GeofenceService(LocationService locationService, double geofenceRadius) {
        this.locationService = locationService;
        this.geofenceRadius = geofenceRadius;
    }

    /**
     * Verify that the app has launched successfully.
     */
    public boolean waitForAppToLoad(GeofenceHomePage homePage, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.debug("Checking if app is loaded (attempt {}/{})", attempt, maxRetries);

            if (homePage.isPageLoaded()) {
                log.info("App loaded successfully");
                return true;
            }

            pause(2000);
        }

        log.warn("App failed to load after {} attempts", maxRetries);
        return false;
    }

    /**
     * Simulate a device entering the geofence area.
     */
    public void simulateGeofenceEntry(GeoLocation center, GeoLocation outsidePoint) {
        log.info("Simulating geofence entry from outside to center");
        locationService.simulateMovement(outsidePoint, center);
    }

    /**
     * Simulate a device exiting the geofence area.
     */
    public void simulateGeofenceExit(GeoLocation center, GeoLocation outsidePoint) {
        log.info("Simulating geofence exit from center to outside");
        locationService.simulateMovement(center, outsidePoint);
    }

    /**
     * Place device at a specific location and verify geofence status.
     */
    public GeofenceStatus placeDeviceAt(GeoLocation location, GeoLocation geofenceCenter) {
        locationService.setLocation(location);

        double distance = locationService.calculateDistance(geofenceCenter, location);
        boolean isInside = locationService.isInsideGeofence(geofenceCenter, location, geofenceRadius);

        log.info("Device at {} - Distance from center: {:.2f}m - Inside geofence: {}",
                location, distance, isInside);

        return new GeofenceStatus(isInside, distance);
    }

    /**
     * Verify the device is inside the geofence.
     */
    public boolean verifyInsideGeofence(GeoLocation center, GeoLocation currentLocation) {
        return locationService.isInsideGeofence(center, currentLocation, geofenceRadius);
    }

    /**
     * Verify the device is outside the geofence.
     */
    public boolean verifyOutsideGeofence(GeoLocation center, GeoLocation currentLocation) {
        return !verifyInsideGeofence(center, currentLocation);
    }

    public double getGeofenceRadius() {
        return geofenceRadius;
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Represents the current geofence status for a device.
     */
    public record GeofenceStatus(boolean isInside, double distanceFromCenter) {

        public boolean isOutside() {
            return !isInside;
        }
    }
}
