package com.geofence.api;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

public class BaseApiTest {

    protected RequestSpecification requestSpec;

    protected static final String BASE_URI = "https://jsonplaceholder.typicode.com";

    @BeforeClass
    public void setupApi() {
        System.out.println("SETTING UP API TEST CONFIGURATION");

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setContentType(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new AllureRestAssured())
                .build();

        RestAssured.requestSpecification = requestSpec;

        System.out.println("API TEST CONFIGURATION SETUP COMPLETE");
    }
}
