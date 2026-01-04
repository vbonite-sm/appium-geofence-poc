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

    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

    @JsonProperty("userId")
    private int userId;

    public GeoFenceRequest() {
    }

    private GeoFenceRequest(Builder builder) {
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.radius = builder.radius;
        this.name = builder.name;
        this.title = builder.title;
        this.body = builder.body;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public static GeoFenceRequest createDefault() {
        return builder()
                .title("Home GeoFence")
                .body("Default geofence for home location")
                .userId(1)
                .latitude(14.5995)
                .longitude(120.9842)
                .radius(100.0)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private double latitude;
        private double longitude;
        private double radius;
        private String name;
        private String title;
        private String body;
        private int userId;

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

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder userId(int userId) {
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
