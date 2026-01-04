package com.geofence.api;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
@Epic("GeoFence API")
@Feature("GeoFence CRUD Operations")
public class GeofenceApiTest extends  BaseApiTest {

//    private static String createGeoFenceId;

    @Test(priority = 1, description = "API-001: Create a new GeoFence and verify creation")
    @Story("Create GeoFence")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Create a new GeoFence via POST and verify it was created successfully")
    public void testCreateGeoFence() {
        System.out.println("--- API-001: Create a new GeoFence and verify creation ---");

        // Arrange - Prepare GeoFence creation payload
        String geoFencePayload = """
                {
                    "title": "Home GeoFence",
                    "body": "GeoFence for tracking location",
                    "userId": 1,
                    "latitude": 37.7749,
                    "longitude": -122.4194,
                    "radius": 100,
                    "alertMessage": "Out of the GeoFence area."
                }
                """;

        // Act - Send POST request to create GeoFence
        Response response = given()
                .spec(requestSpec)
                .body(geoFencePayload)
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .body("title", equalTo("Home GeoFence"))
                .body("userId", equalTo(1))
                .body("id", notNullValue())
                .extract().response();

        // Assert - Verify response and store created GeoFence ID
        String createGeoFenceId = response.jsonPath().getString("id");
        Assert.assertNotNull(createGeoFenceId, "Created GeoFence ID should not be null");
        Assert.assertTrue(Integer.parseInt(createGeoFenceId) > 0, "Created GeoFence ID should be greater than 0");

        // Log created GeoFence ID
        System.out.println("Created GeoFence with ID: " + createGeoFenceId);
        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("--- API-001 PASSED ---");
    }

    @Test(priority = 2, description = "API-002: Get the created GeoFence and verify details")
    @Story("Get GeoFence")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Retrieve the created GeoFence via GET and verify its details")
    public void testGetGeoFence() {
        System.out.println("--- API-002: Get the created GeoFence and verify details ---");

        // Arrange - Ensure we have a GeoFence ID to retrieve
        int geoFenceId = 1;

        // Act - Send GET request to retrieve GeoFence
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

        // Assert Verify response details
        int retrievedId = response.jsonPath().getInt("id");
        String title = response.jsonPath().getString("title");
        int userId = response.jsonPath().getInt("userId");

        Assert.assertEquals(retrievedId, geoFenceId, "Retrieved GeoFence ID should match requested ID");
        Assert.assertNotNull(title, "GeoFence title should not be null");
        Assert.assertTrue(userId > 0, "GeoFence userId should be greater than 0");

        // Log retrieved GeoFence details
        System.out.println("Retrieved GeoFence with ID: " + retrievedId);
        System.out.println("Title: " + title);
        System.out.println("User ID: " + userId);
        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("--- API-002 PASSED ---");
    }
}
