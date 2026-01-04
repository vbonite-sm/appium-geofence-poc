package com.geofence.models;

import java.util.Objects;

/**
 * Represents a geographic location with latitude and longitude.
 * Includes utility methods for distance calculation using the Haversine formula.
 */
public class GeoLocation {

    private static final double EARTH_RADIUS_METERS = 6371000;

    private final double latitude;
    private final double longitude;
    private final String name;
    private final Double altitude;

    private GeoLocation(Builder builder) {
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.name = builder.name;
        this.altitude = builder.altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public Double getAltitude() {
        return altitude;
    }

    /**
     * Calculate distance to another location using Haversine formula.
     * @return distance in meters
     */
    public double distanceTo(GeoLocation other) {
        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLat = Math.toRadians(other.latitude - this.latitude);
        double deltaLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Check if this location is within the given radius of a center point.
     */
    public boolean isWithinRadius(GeoLocation center, double radiusMeters) {
        return distanceTo(center) <= radiusMeters;
    }

    /**
     * Create a new location offset from this one by the given meters.
     */
    public GeoLocation offset(double northMeters, double eastMeters) {
        double deltaLat = northMeters / EARTH_RADIUS_METERS;
        double deltaLon = eastMeters / (EARTH_RADIUS_METERS * Math.cos(Math.toRadians(latitude)));

        return new Builder()
                .latitude(latitude + Math.toDegrees(deltaLat))
                .longitude(longitude + Math.toDegrees(deltaLon))
                .name(name != null ? name + " (offset)" : null)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoLocation that = (GeoLocation) o;
        return Double.compare(latitude, that.latitude) == 0
                && Double.compare(longitude, that.longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        String display = name != null ? name + " " : "";
        return String.format("%s(%.6f, %.6f)", display, latitude, longitude);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static GeoLocation of(double latitude, double longitude) {
        return new Builder().latitude(latitude).longitude(longitude).build();
    }

    public static class Builder {
        private double latitude;
        private double longitude;
        private String name;
        private Double altitude;

        public Builder latitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder altitude(Double altitude) {
            this.altitude = altitude;
            return this;
        }

        public GeoLocation build() {
            return new GeoLocation(this);
        }
    }
}
