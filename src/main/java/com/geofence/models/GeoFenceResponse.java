package com.geofence.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for geofence API operations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoFenceResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("inside")
    private Boolean inside;

    @JsonProperty("distance")
    private Double distance;

    @JsonProperty("geofenceName")
    private String geofenceName;

    public GeoFenceResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getInside() {
        return inside;
    }

    public void setInside(Boolean inside) {
        this.inside = inside;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getGeofenceName() {
        return geofenceName;
    }

    public void setGeofenceName(String geofenceName) {
        this.geofenceName = geofenceName;
    }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status) || "ok".equalsIgnoreCase(status);
    }

    public boolean isInsideGeofence() {
        return Boolean.TRUE.equals(inside);
    }

    @Override
    public String toString() {
        return String.format("GeoFenceResponse{id='%s', status='%s', inside=%s, distance=%s}",
                id, status, inside, distance);
    }
}
