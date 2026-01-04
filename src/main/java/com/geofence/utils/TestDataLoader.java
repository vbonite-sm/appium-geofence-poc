package com.geofence.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geofence.models.GeoFenceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Utility class for loading test data from JSON files.
 */
public class TestDataLoader {

    private static final Logger log = LoggerFactory.getLogger(TestDataLoader.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TEST_DATA_PATH = "testdata/";

    private TestDataLoader() {
    }

    public static <T> T loadJson(String filename, Class<T> clazz) {
        String path = TEST_DATA_PATH + filename;
        try (InputStream is = TestDataLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("File not found: " + path);
            }
            return objectMapper.readValue(is, clazz);
        } catch (Exception e) {
            log.error("Failed to load JSON from {}: {}", path, e.getMessage());
            throw new RuntimeException("Failed to load test data: " + filename, e);
        }
    }

    public static <T> T loadJson(String filename, TypeReference<T> typeRef) {
        String path = TEST_DATA_PATH + filename;
        try (InputStream is = TestDataLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("File not found: " + path);
            }
            return objectMapper.readValue(is, typeRef);
        } catch (Exception e) {
            log.error("Failed to load JSON from {}: {}", path, e.getMessage());
            throw new RuntimeException("Failed to load test data: " + filename, e);
        }
    }

    public static JsonNode loadJsonNode(String filename) {
        String path = TEST_DATA_PATH + filename;
        try (InputStream is = TestDataLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("File not found: " + path);
            }
            return objectMapper.readTree(is);
        } catch (Exception e) {
            log.error("Failed to load JSON node from {}: {}", path, e.getMessage());
            throw new RuntimeException("Failed to load test data: " + filename, e);
        }
    }

    public static GeoFenceRequest loadApiPayload(String payloadName) {
        JsonNode root = loadJsonNode("api-payloads.json");
        JsonNode payload = root.get(payloadName);

        if (payload == null) {
            throw new IllegalArgumentException("Payload not found: " + payloadName);
        }

        return GeoFenceRequest.builder()
                .title(payload.path("title").asText())
                .body(payload.path("body").asText())
                .userId(payload.path("userId").asInt())
                .latitude(payload.path("latitude").asDouble())
                .longitude(payload.path("longitude").asDouble())
                .radius(payload.path("radius").asDouble())
                .name(payload.path("alertMessage").asText())
                .build();
    }
}
