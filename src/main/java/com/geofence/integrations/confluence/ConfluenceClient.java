package com.geofence.integrations.confluence;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Confluence REST API client for creating and updating documentation pages.
 * Uses the same Atlassian credentials as Jira.
 */
public class ConfluenceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfluenceClient.class);
    
    private final String baseUrl;
    private final String authHeader;
    private final String spaceKey;
    
    public ConfluenceClient(String baseUrl, String email, String apiToken, String spaceKey) {
        // Ensure /wiki is in the URL but not duplicated
        if (baseUrl.endsWith("/wiki")) {
            this.baseUrl = baseUrl;
        } else if (baseUrl.contains("/wiki")) {
            this.baseUrl = baseUrl;
        } else {
            this.baseUrl = baseUrl.replace("atlassian.net", "atlassian.net/wiki");
        }
        this.spaceKey = spaceKey;
        String credentials = email + ":" + apiToken;
        this.authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
    
    private RequestSpecification getBaseRequest() {
        return RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", authHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }
    
    /**
     * Creates a new Confluence page
     * 
     * @param title Page title
     * @param content Page content in storage format (HTML-like)
     * @param parentPageId Optional parent page ID
     * @return Created page ID
     */
    public String createPage(String title, String content, String parentPageId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "page");
        payload.put("title", title);
        payload.put("space", Map.of("key", spaceKey));
        payload.put("body", Map.of(
            "storage", Map.of(
                "value", content,
                "representation", "storage"
            )
        ));
        
        if (parentPageId != null && !parentPageId.isEmpty()) {
            payload.put("ancestors", new Object[]{Map.of("id", parentPageId)});
        }
        
        try {
            Response response = getBaseRequest()
                    .body(payload)
                    .post("/rest/api/content");
            
            if (response.getStatusCode() == 200) {
                String pageId = response.jsonPath().getString("id");
                logger.info("Created Confluence page: {}", pageId);
                return pageId;
            } else {
                logger.error("Failed to create page: {}", response.getStatusCode());
                logger.error("Response: {}", response.asString());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error creating Confluence page: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Creates a test execution report page in Confluence
     */
    public String createTestReportPage(String suiteName, int passed, int failed, int skipped, 
                                        String allureReportUrl) {
        String title = "Test Report: " + suiteName + " - " + 
                       java.time.LocalDate.now().toString();
        
        String content = String.format("""
            <h1>Test Execution Report</h1>
            <h2>Suite: %s</h2>
            <p><strong>Execution Date:</strong> %s</p>
            <h3>Results Summary</h3>
            <table>
            <tr><th>Status</th><th>Count</th></tr>
            <tr><td style='color:green'>✅ Passed</td><td>%d</td></tr>
            <tr><td style='color:red'>❌ Failed</td><td>%d</td></tr>
            <tr><td style='color:orange'>⏭️ Skipped</td><td>%d</td></tr>
            <tr><td><strong>Total</strong></td><td><strong>%d</strong></td></tr>
            </table>
            <h3>Pass Rate</h3>
            <p><strong>%.1f%%</strong></p>
            <h3>Detailed Report</h3>
            <p><a href='%s'>View Allure Report</a></p>
            """,
            suiteName,
            java.time.LocalDateTime.now().toString(),
            passed, failed, skipped, (passed + failed + skipped),
            (passed * 100.0 / (passed + failed + skipped)),
            allureReportUrl
        );
        
        return createPage(title, content, null);
    }
    
    /**
     * Updates an existing Confluence page
     */
    public boolean updatePage(String pageId, String title, String content) {
        // First, get current page version
        Response getResponse = getBaseRequest()
                .get("/rest/api/content/" + pageId);
        
        if (getResponse.getStatusCode() != 200) {
            logger.error("Failed to get page: {}", getResponse.getStatusCode());
            return false;
        }
        
        int currentVersion = getResponse.jsonPath().getInt("version.number");
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "page");
        payload.put("title", title);
        payload.put("body", Map.of(
            "storage", Map.of(
                "value", content,
                "representation", "storage"
            )
        ));
        payload.put("version", Map.of("number", currentVersion + 1));
        
        try {
            Response response = getBaseRequest()
                    .body(payload)
                    .put("/rest/api/content/" + pageId);
            
            return response.getStatusCode() == 200;
        } catch (Exception e) {
            logger.error("Error updating page: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets page content by ID
     */
    public Map<String, Object> getPage(String pageId) {
        try {
            Response response = getBaseRequest()
                    .queryParam("expand", "body.storage,version")
                    .get("/rest/api/content/" + pageId);
            
            if (response.getStatusCode() == 200) {
                return response.jsonPath().getMap("");
            }
        } catch (Exception e) {
            logger.error("Error getting page: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Searches for pages in the space
     */
    public Response searchPages(String query) {
        try {
            return getBaseRequest()
                    .queryParam("cql", "space=" + spaceKey + " and text~\"" + query + "\"")
                    .get("/rest/api/content/search");
        } catch (Exception e) {
            logger.error("Error searching pages: {}", e.getMessage());
            return null;
        }
    }
}
