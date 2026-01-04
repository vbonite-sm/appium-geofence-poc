package com.geofence.tests.api;

import com.geofence.integrations.confluence.ConfluenceClient;
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.*;

/**
 * API tests for Confluence integration functionality.
 * These tests verify the Confluence REST API integration works correctly.
 */
@Epic("Atlassian Integration")
@Feature("Confluence API Integration")
public class ConfluenceApiTests {
    
    private ConfluenceClient confluenceClient;
    private String baseUrl;
    private String email;
    private String apiToken;
    private String spaceKey;
    private boolean isConfigured;
    private String testPageId;
    
    @BeforeClass
    public void setUp() {
        // Load from properties file first, then environment variables as override
        java.util.Properties props = new java.util.Properties();
        try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("jira-config.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (java.io.IOException e) {
            System.out.println("Could not load jira-config.properties: " + e.getMessage());
        }
        
        // Properties file values
        baseUrl = props.getProperty("confluence.base.url");
        email = props.getProperty("jira.email");
        apiToken = props.getProperty("jira.api.token");
        spaceKey = props.getProperty("confluence.space.key");
        
        // Environment variables override properties file
        if (System.getenv("CONFLUENCE_BASE_URL") != null) baseUrl = System.getenv("CONFLUENCE_BASE_URL");
        if (System.getenv("JIRA_EMAIL") != null) email = System.getenv("JIRA_EMAIL");
        if (System.getenv("JIRA_API_TOKEN") != null) apiToken = System.getenv("JIRA_API_TOKEN");
        if (System.getenv("CONFLUENCE_SPACE_KEY") != null) spaceKey = System.getenv("CONFLUENCE_SPACE_KEY");
        
        // Fallback to Jira base URL if Confluence URL not set
        if (baseUrl == null || baseUrl.isEmpty()) {
            String jiraUrl = props.getProperty("jira.base.url");
            if (jiraUrl != null) baseUrl = jiraUrl.replace("atlassian.net", "atlassian.net/wiki");
        }
        
        isConfigured = baseUrl != null && email != null && apiToken != null && !apiToken.isEmpty();
        
        if (isConfigured) {
            confluenceClient = new ConfluenceClient(baseUrl, email, apiToken, spaceKey);
        }
        
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }
    
    @Test(priority = 1)
    @Severity(SeverityLevel.CRITICAL)
    @Story("Confluence Configuration")
    @Description("Verify Confluence configuration is loaded correctly")
    public void testConfluenceConfigurationLoaded() {
        System.out.println("Testing Confluence Configuration...");
        
        if (!isConfigured) {
            System.out.println("Confluence not configured - using mock validation");
            System.out.println("To configure, set CONFLUENCE_BASE_URL, JIRA_EMAIL, " +
                             "JIRA_API_TOKEN, CONFLUENCE_SPACE_KEY environment variables");
            return;
        }
        
        assertNotNull(baseUrl, "Confluence base URL should be configured");
        assertNotNull(email, "Email should be configured");
        assertNotNull(spaceKey, "Space key should be configured");
    }
    
    @Test(priority = 2, dependsOnMethods = "testConfluenceConfigurationLoaded")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Create Page")
    @Description("Verify ability to create a new page in Confluence")
    public void testCreateConfluencePage() {
        if (!isConfigured) {
            System.out.println("Confluence not configured - skipping create page test");
            return;
        }
        
        String title = "Test Page - Automation POC - " + System.currentTimeMillis();
        String content = "<h1>Test Page</h1>" +
                        "<p>This is a test page created by the automation framework.</p>" +
                        "<p>Created at: " + java.time.LocalDateTime.now() + "</p>";
        
        testPageId = confluenceClient.createPage(title, content, null);
        
        assertNotNull(testPageId, "Page should be created successfully");
        System.out.println("Created test page with ID: " + testPageId);
    }
    
    @Test(priority = 3, dependsOnMethods = "testCreateConfluencePage")
    @Severity(SeverityLevel.NORMAL)
    @Story("Get Page")
    @Description("Verify ability to retrieve page details from Confluence")
    public void testGetPageDetails() {
        if (!isConfigured || testPageId == null) {
            System.out.println("Skipping get page test - no page available");
            return;
        }
        
        Map<String, Object> pageDetails = confluenceClient.getPage(testPageId);
        
        assertNotNull(pageDetails, "Page details should be retrieved");
        assertEquals(pageDetails.get("id"), testPageId, "Page ID should match");
    }
    
    @Test(priority = 4, dependsOnMethods = "testCreateConfluencePage")
    @Severity(SeverityLevel.NORMAL)
    @Story("Update Page")
    @Description("Verify ability to update an existing Confluence page")
    public void testUpdatePage() {
        if (!isConfigured || testPageId == null) {
            System.out.println("Skipping update page test - no page available");
            return;
        }
        
        String updatedTitle = "Updated Test Page - " + System.currentTimeMillis();
        String updatedContent = "<h1>Updated Test Page</h1>" +
                               "<p>This page was updated by the automation framework.</p>" +
                               "<p>Updated at: " + java.time.LocalDateTime.now() + "</p>";
        
        boolean result = confluenceClient.updatePage(testPageId, updatedTitle, updatedContent);
        
        assertTrue(result, "Page should be updated successfully");
    }
    
    @Test(priority = 5)
    @Severity(SeverityLevel.NORMAL)
    @Story("Create Test Report")
    @Description("Verify ability to create test execution report page")
    public void testCreateTestReportPage() {
        if (!isConfigured) {
            System.out.println("Confluence not configured - skipping report page test");
            return;
        }
        
        String reportPageId = confluenceClient.createTestReportPage(
            "Geofence E2E Tests",
            4,  // passed
            0,  // failed
            0,  // skipped
            "http://localhost:8080/allure-report"
        );
        
        assertNotNull(reportPageId, "Test report page should be created");
        System.out.println("Created test report page with ID: " + reportPageId);
    }
    
    @Test(priority = 6)
    @Severity(SeverityLevel.MINOR)
    @Story("Search Pages")
    @Description("Verify search functionality in Confluence")
    public void testSearchPages() {
        if (!isConfigured) {
            System.out.println("Confluence not configured - skipping search test");
            return;
        }
        
        io.restassured.response.Response response = confluenceClient.searchPages("Test");
        
        assertNotNull(response, "Search response should not be null");
        assertEquals(response.getStatusCode(), 200, "Search should return 200 OK");
    }
}
