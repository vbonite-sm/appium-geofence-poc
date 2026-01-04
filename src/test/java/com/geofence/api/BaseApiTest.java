package com.geofence.api;

import com.geofence.config.EnvironmentConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseApiTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseApiTest.class);
    protected RequestSpecification requestSpec;
    protected EnvironmentConfig config;

    @BeforeClass
    public void setupApi() {
        config = EnvironmentConfig.getInstance();
        String baseUri = config.getApiBaseUri();

        log.info("Configuring API tests with base URI: {}", baseUri);

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setContentType(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new AllureRestAssured())
                .build();

        RestAssured.requestSpecification = requestSpec;
    }
}
