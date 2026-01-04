package com.geofence.dataproviders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geofence.models.GeoLocation;
import org.testng.annotations.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Data providers for geofence test scenarios.
 */
public class GeofenceDataProvider {

    private static final Logger log = LoggerFactory.getLogger(GeofenceDataProvider.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Provides predefined test locations.
     */
    @DataProvider(name = "testLocations")
    public static Object[][] testLocations() {
        return new Object[][] {
            { TestLocations.GEOFENCE_CENTER, "Center", true },
            { TestLocations.INSIDE_50M, "Inside 50m", true },
            { TestLocations.ON_BOUNDARY_100M, "On Boundary", true },
            { TestLocations.OUTSIDE_150M, "Outside 150m", false },
            { TestLocations.OUTSIDE_200M, "Outside 200m", false }
        };
    }

    /**
     * Provides entry/exit scenarios.
     */
    @DataProvider(name = "geofenceTransitions")
    public static Object[][] geofenceTransitions() {
        return new Object[][] {
            { TestLocations.OUTSIDE_150M, TestLocations.GEOFENCE_CENTER, "Entry from outside" },
            { TestLocations.GEOFENCE_CENTER, TestLocations.OUTSIDE_150M, "Exit to 150m" },
            { TestLocations.INSIDE_50M, TestLocations.OUTSIDE_200M, "Exit to 200m" }
        };
    }

    /**
     * Loads test data from JSON file.
     */
    @DataProvider(name = "locationsFromJson")
    public static Object[][] locationsFromJson() {
        try (InputStream is = GeofenceDataProvider.class.getClassLoader()
                .getResourceAsStream("testdata/locations.json")) {

            if (is == null) {
                log.warn("locations.json not found, using defaults");
                return testLocations();
            }

            List<Map<String, Object>> locations = objectMapper.readValue(
                    is, new TypeReference<List<Map<String, Object>>>() {});

            return locations.stream()
                    .map(loc -> new Object[] {
                            GeoLocation.builder()
                                    .latitude(((Number) loc.get("latitude")).doubleValue())
                                    .longitude(((Number) loc.get("longitude")).doubleValue())
                                    .name((String) loc.get("name"))
                                    .build(),
                            loc.get("name"),
                            loc.get("expectedInside")
                    })
                    .toArray(Object[][]::new);

        } catch (Exception e) {
            log.error("Failed to load locations from JSON", e);
            return testLocations();
        }
    }

    /**
     * Predefined test locations for San Francisco geofence.
     */
    public static class TestLocations {
        public static final GeoLocation GEOFENCE_CENTER = GeoLocation.builder()
                .latitude(37.7749)
                .longitude(-122.4194)
                .name("Geofence Center (SF)")
                .build();

        public static final GeoLocation INSIDE_50M = GeoLocation.builder()
                .latitude(37.7753)
                .longitude(-122.4194)
                .name("Inside 50m")
                .build();

        public static final GeoLocation ON_BOUNDARY_100M = GeoLocation.builder()
                .latitude(37.77579)
                .longitude(-122.4194)
                .name("On Boundary 100m")
                .build();

        public static final GeoLocation OUTSIDE_150M = GeoLocation.builder()
                .latitude(37.7763)
                .longitude(-122.4194)
                .name("Outside 150m")
                .build();

        public static final GeoLocation OUTSIDE_200M = GeoLocation.builder()
                .latitude(37.7767)
                .longitude(-122.4194)
                .name("Outside 200m")
                .build();

        public static final GeoLocation NEW_YORK = GeoLocation.builder()
                .latitude(40.7128)
                .longitude(-74.0060)
                .name("New York")
                .build();
    }
}
