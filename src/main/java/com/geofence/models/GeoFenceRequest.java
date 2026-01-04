package com.geofence.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request model for geofence API operations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoFenceRequest {

    @JsonProperty("latitude")
    private double latitude;

    @JsonProperty("longitude")
    private double longitude;

    @JsonProperty("radius")
    private double radius;

    @JsonProperty("name")
    private String name;

    @JsonProperty("userId")
    private String userId;

    public GeoFenceRequest() {
    }

    private GeoFenceRequest(Builder builder) {
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.radius = builder.radius;
        this.name = builder.name;
        this.userId = builder.userId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private double latitude;
        private double longitude;
        private double radius;
        private String name;
        private String userId;

        public Builder latitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder radius(double radius) {
            this.radius = radius;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder fromLocation(GeoLocation location) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
            if (location.getName() != null) {
                this.name = location.getName();
            }
            return this;
        }

        public GeoFenceRequest build() {
            return new GeoFenceRequest(this);
        }
    }
}
