package com.geofence.api;

import com.geofence.models.GeoFenceRequest;
import com.geofence.models.GeoFenceResponse;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("GeoFence API")
@Feature("GeoFence CRUD Operations")
public class GeofenceApiTest extends BaseApiTest {

    @Test(priority = 1, description = "API-001: Create a new GeoFence and verify creation")
    @Story("Create GeoFence")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Create a new GeoFence via POST and verify it was created successfully")
    public void testCreateGeoFence() {
        log.info("API-001: Create a new GeoFence and verify creation");

        // Arrange
        GeoFenceRequest payload = GeoFenceRequest.createDefault();

        // Act
        Response response = given()
                .spec(requestSpec)
                .body(payload)
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .body("title", equalTo(payload.getTitle()))
                .body("userId", equalTo(payload.getUserId()))
                .body("id", notNullValue())
                .extract().response();

        GeoFenceResponse created = response.as(GeoFenceResponse.class);

        // Assert
        Assert.assertNotNull(created.getId(), "Created GeoFence ID should not be null");
        Assert.assertTrue(created.getId() > 0, "Created GeoFence ID should be greater than 0");
        log.info("Created GeoFence with ID: {}", created.getId());
        Allure.step("Created GeoFence: " + created);
    }

    @Test(priority = 2, description = "API-002: Get the created GeoFence and verify details")
    @Story("Get GeoFence")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Retrieve a GeoFence via GET and verify its details")
    public void testGetGeoFence() {
        log.info("API-002: Get a GeoFence and verify details");

        // Arrange
        int geoFenceId = 1;

        // Act
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", geoFenceId)
                .when()
                .get("/posts/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(geoFenceId))
                .body("userId", notNullValue())
                .body("title", notNullValue())
                .body("body", notNullValue())
                .extract().response();

        GeoFenceResponse retrieved = response.as(GeoFenceResponse.class);

        // Assert
        Assert.assertEquals(retrieved.getId().intValue(), geoFenceId);
        Assert.assertNotNull(retrieved.getTitle());
        Assert.assertTrue(retrieved.getUserId() > 0);
        log.info("Retrieved GeoFence: {}", retrieved);
        Allure.step("Retrieved GeoFence details successfully");
    }

    @Test(priority = 3, description = "API-003: Update an existing GeoFence")
    @Story("Update GeoFence")
    @Severity(SeverityLevel.NORMAL)
    @Description("Update a GeoFence via PUT and verify changes")
    public void testUpdateGeoFence() {
        log.info("API-003: Update an existing GeoFence");

        // Arrange
        int geoFenceId = 1;
        GeoFenceRequest updatePayload = GeoFenceRequest.builder()
                .title("Updated Home GeoFence")
                .body("Updated description")
                .userId(1)
                .radius(150.0)
                .build();

        // Act
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", geoFenceId)
                .body(updatePayload)
                .when()
                .put("/posts/{id}")
                .then()
                .statusCode(200)
                .body("title", equalTo(updatePayload.getTitle()))
                .extract().response();

        GeoFenceResponse updated = response.as(GeoFenceResponse.class);

        // Assert
        Assert.assertEquals(updated.getTitle(), updatePayload.getTitle());
        log.info("Updated GeoFence: {}", updated);
    }

    @Test(priority = 4, description = "API-004: Delete a GeoFence")
    @Story("Delete GeoFence")
    @Severity(SeverityLevel.NORMAL)
    @Description("Delete a GeoFence via DELETE")
    public void testDeleteGeoFence() {
        log.info("API-004: Delete a GeoFence");

        // Arrange
        int geoFenceId = 1;

        // Act & Assert
        given()
                .spec(requestSpec)
                .pathParam("id", geoFenceId)
                .when()
                .delete("/posts/{id}")
                .then()
                .statusCode(200);

        log.info("GeoFence {} deleted successfully", geoFenceId);
    }
}
