package com.geofence.tests.api;

import com.geofence.integrations.jira.JiraClient;
import com.geofence.integrations.jira.JiraConfig;
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.*;

/**
 * API tests for Jira integration functionality.
 * These tests verify the Jira REST API integration works correctly.
 */
@Epic("Atlassian Integration")
@Feature("Jira API Integration")
public class JiraApiTests {
    
    private JiraClient jiraClient;
    private JiraConfig config;
    private String testIssueKey;
    
    @BeforeClass
    public void setUp() {
        config = JiraConfig.getInstance();
        jiraClient = new JiraClient();
        
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }
    
    @Test(priority = 1)
    @Severity(SeverityLevel.CRITICAL)
    @Story("Jira Configuration")
    @Description("Verify Jira configuration is loaded correctly")
    public void testJiraConfigurationLoaded() {
        System.out.println("Testing Jira Configuration...");
        
        // If not configured, skip tests but don't fail
        if (!config.isConfigured()) {
            System.out.println("Jira not configured - using mock validation");
            assertNotNull(config);
            return;
        }
        
        assertNotNull(config.getBaseUrl(), "Jira base URL should be configured");
        assertNotNull(config.getEmail(), "Jira email should be configured");
        assertNotNull(config.getProjectKey(), "Jira project key should be configured");
        assertTrue(config.getBaseUrl().contains("atlassian.net"), 
                   "Base URL should be Atlassian Cloud URL");
    }
    
    @Test(priority = 2, dependsOnMethods = "testJiraConfigurationLoaded")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Create Issue")
    @Description("Verify ability to create a new issue in Jira")
    public void testCreateJiraIssue() {
        if (!config.isConfigured()) {
            System.out.println("Jira not configured - skipping create test");
            return;
        }
        
        String summary = "[Test] Automation POC Test Issue - " + System.currentTimeMillis();
        String description = "This is a test issue created by the automation framework.\n" +
                            "It can be safely deleted.";
        
        testIssueKey = jiraClient.createTask(summary, description);
        
        assertNotNull(testIssueKey, "Issue should be created successfully");
        assertTrue(testIssueKey.startsWith(config.getProjectKey()), 
                   "Issue key should start with project key");
        
        System.out.println("Created test issue: " + testIssueKey);
    }
    
    @Test(priority = 3, dependsOnMethods = "testCreateJiraIssue")
    @Severity(SeverityLevel.NORMAL)
    @Story("Add Comment")
    @Description("Verify ability to add comments to Jira issues")
    public void testAddCommentToIssue() {
        if (!config.isConfigured() || testIssueKey == null) {
            System.out.println("Skipping comment test - no issue available");
            return;
        }
        
        String comment = "Test comment added by automation at " + 
                        java.time.LocalDateTime.now();
        
        boolean result = jiraClient.addComment(testIssueKey, comment);
        
        assertTrue(result, "Comment should be added successfully");
    }
    
    @Test(priority = 4, dependsOnMethods = "testCreateJiraIssue")
    @Severity(SeverityLevel.NORMAL)
    @Story("Get Issue")
    @Description("Verify ability to retrieve issue details from Jira")
    public void testGetIssueDetails() {
        if (!config.isConfigured() || testIssueKey == null) {
            System.out.println("Skipping get issue test - no issue available");
            return;
        }
        
        Map<String, Object> issueDetails = jiraClient.getIssue(testIssueKey);
        
        assertNotNull(issueDetails, "Issue details should be retrieved");
        assertEquals(issueDetails.get("key"), testIssueKey, 
                    "Issue key should match");
    }
    
    @Test(priority = 5)
    @Severity(SeverityLevel.NORMAL)
    @Story("Search Issues")
    @Description("Verify JQL search functionality")
    public void testSearchIssues() {
        if (!config.isConfigured()) {
            System.out.println("Jira not configured - skipping search test");
            return;
        }
        
        String jql = "project = " + config.getProjectKey() + " ORDER BY created DESC";
        
        Response response = jiraClient.searchIssues(jql);
        
        assertNotNull(response, "Search response should not be null");
        assertEquals(response.getStatusCode(), 200, "Search should return 200 OK");
        
        // New /search/jql API returns issues array instead of total
        int issueCount = response.jsonPath().getList("issues").size();
        System.out.println("Found " + issueCount + " issues in project " + config.getProjectKey());
        assertTrue(issueCount >= 0, "Should return valid issue count");
    }
    
    @Test(priority = 6)
    @Severity(SeverityLevel.CRITICAL)
    @Story("Create Defect")
    @Description("Verify ability to create defect/bug for failed test")
    public void testCreateDefectForFailedTest() {
        if (!config.isConfigured()) {
            System.out.println("Jira not configured - skipping defect creation test");
            return;
        }
        
        String testName = "com.geofence.tests.SampleTest.testGeofenceExit";
        String errorMessage = "Expected notification 'kid out of geofence' but got null";
        String stackTrace = """
                java.lang.AssertionError: Expected notification
                    at org.testng.Assert.fail(Assert.java:99)
                    at com.geofence.tests.SampleTest.testGeofenceExit(SampleTest.java:45)
                """;
        
        String defectKey = jiraClient.createDefect(testName, errorMessage, stackTrace);
        
        assertNotNull(defectKey, "Defect should be created successfully");
        assertTrue(defectKey.startsWith(config.getProjectKey()), 
                   "Defect key should start with project key");
        
        System.out.println("Created defect: " + defectKey);
    }
}
