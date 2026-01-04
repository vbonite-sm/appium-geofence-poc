package com.geofence.integrations.jira;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jira REST API client for creating issues, adding comments, and managing defects.
 * Uses REST Assured for API communication.
 */
public class JiraClient {
    
    private final JiraConfig config;
    private final String authHeader;
    private final String baseUrl;
    
    public JiraClient() {
        this.config = JiraConfig.getInstance();
        this.authHeader = createAuthHeader();
        this.baseUrl = config.getBaseUrl();
    }
    
    /**
     * Constructor with explicit credentials (for CI/CD integration)
     */
    public JiraClient(String baseUrl, String email, String apiToken) {
        this.config = null;
        this.baseUrl = baseUrl;
        String credentials = email + ":" + apiToken;
        this.authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
    
    private String createAuthHeader() {
        String credentials = config.getEmail() + ":" + config.getApiToken();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
    
    private RequestSpecification getBaseRequest() {
        return RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", authHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }
    
    private String getProjectKey() {
        return config != null ? config.getProjectKey() : System.getenv("JIRA_PROJECT_KEY");
    }
    
    private boolean isConfigured() {
        return config != null ? config.isConfigured() : (baseUrl != null && authHeader != null);
    }
    
    /**
     * Creates a new Jira issue (Bug/Defect)
     * 
     * @param summary Issue summary/title
     * @param description Issue description
     * @param issueType Issue type (Bug, Task, Story)
     * @return Created issue key (e.g., GEO-123)
     */
    public String createIssue(String summary, String description, String issueType) {
        return createIssue(getProjectKey(), issueType, summary, description, null);
    }
    
    /**
     * Creates a new Jira issue with labels (for CI/CD integration)
     * 
     * @param projectKey Project key (e.g., GEO)
     * @param issueType Issue type (Bug, Task, Story)
     * @param summary Issue summary/title
     * @param description Issue description
     * @param labels List of labels to add
     * @return Created issue key (e.g., GEO-123)
     */
    public String createIssue(String projectKey, String issueType, String summary, String description, List<String> labels) {
        if (!isConfigured()) {
            System.out.println("Jira not configured. Skipping issue creation.");
            return null;
        }
        
        Map<String, Object> fields = new HashMap<>();
        fields.put("project", Map.of("key", projectKey));
        fields.put("summary", summary);
        fields.put("description", createDescription(description));
        fields.put("issuetype", Map.of("name", issueType));
        
        if (labels != null && !labels.isEmpty()) {
            fields.put("labels", labels);
        }
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("fields", fields);
        
        try {
            Response response = getBaseRequest()
                    .body(payload)
                    .post("/rest/api/3/issue");
            
            if (response.getStatusCode() == 201) {
                String issueKey = response.jsonPath().getString("key");
                System.out.println("Created Jira issue: " + issueKey);
                return issueKey;
            } else {
                System.err.println("Failed to create Jira issue: " + response.getStatusCode());
                System.err.println("Response: " + response.asString());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error creating Jira issue: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Creates a Bug/Defect in Jira for a failed test
     */
    public String createDefect(String testName, String errorMessage, String stackTrace) {
        String summary = "[Automation] Test Failed: " + testName;
        String description = String.format(
            "h2. Test Failure Details\n\n" +
            "*Test Name:* %s\n\n" +
            "*Error Message:*\n{code}%s{code}\n\n" +
            "*Stack Trace:*\n{code}%s{code}\n\n" +
            "*Environment:*\n" +
            "- Framework: Appium Geofence POC\n" +
            "- Execution Time: %s\n",
            testName, errorMessage, stackTrace, java.time.LocalDateTime.now()
        );
        
        return createIssue(summary, description, "Bug");
    }
    
    /**
     * Creates a Task in Jira
     */
    public String createTask(String summary, String description) {
        return createIssue(summary, description, "Task");
    }
    
    /**
     * Adds a comment to an existing Jira issue
     */
    public boolean addComment(String issueKey, String comment) {
        if (!isConfigured()) {
            System.out.println("Jira not configured. Skipping comment.");
            return false;
        }
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("body", createDescription(comment));
        
        try {
            Response response = getBaseRequest()
                    .body(payload)
                    .post("/rest/api/3/issue/" + issueKey + "/comment");
            
            if (response.getStatusCode() == 201) {
                System.out.println("Added comment to issue: " + issueKey);
                return true;
            } else {
                System.err.println("Failed to add comment: " + response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error adding comment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Updates the status of a Jira issue
     */
    public boolean transitionIssue(String issueKey, String transitionName) {
        if (!isConfigured()) {
            return false;
        }
        
        try {
            // First, get available transitions
            Response transitionsResponse = getBaseRequest()
                    .get("/rest/api/3/issue/" + issueKey + "/transitions");
            
            String transitionId = transitionsResponse.jsonPath()
                    .getString("transitions.find { it.name == '" + transitionName + "' }.id");
            
            if (transitionId == null) {
                System.err.println("Transition not found: " + transitionName);
                return false;
            }
            
            // Execute transition
            Map<String, Object> payload = new HashMap<>();
            payload.put("transition", Map.of("id", transitionId));
            
            Response response = getBaseRequest()
                    .body(payload)
                    .post("/rest/api/3/issue/" + issueKey + "/transitions");
            
            return response.getStatusCode() == 204;
        } catch (Exception e) {
            System.err.println("Error transitioning issue: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets issue details by key
     */
    public Map<String, Object> getIssue(String issueKey) {
        if (!isConfigured()) {
            return null;
        }
        
        try {
            Response response = getBaseRequest()
                    .get("/rest/api/3/issue/" + issueKey);
            
            if (response.getStatusCode() == 200) {
                return response.jsonPath().getMap("");
            }
        } catch (Exception e) {
            System.err.println("Error getting issue: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Searches for issues using JQL (using new /search/jql endpoint)
     * @see <a href="https://developer.atlassian.com/changelog/#CHANGE-2046">Jira API Migration</a>
     */
    public Response searchIssues(String jql) {
        if (!isConfigured()) {
            return null;
        }
        
        try {
            // Use POST to /search/jql with JSON body (new API as of 2024)
            Map<String, Object> payload = new HashMap<>();
            payload.put("jql", jql);
            payload.put("maxResults", 50);
            payload.put("fields", new String[]{"summary", "status", "issuetype", "created", "updated"});
            
            return getBaseRequest()
                    .body(payload)
                    .post("/rest/api/3/search/jql");
        } catch (Exception e) {
            System.err.println("Error searching issues: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Creates Atlassian Document Format description
     */
    private Map<String, Object> createDescription(String text) {
        return Map.of(
            "type", "doc",
            "version", 1,
            "content", new Object[]{
                Map.of(
                    "type", "paragraph",
                    "content", new Object[]{
                        Map.of("type", "text", "text", text)
                    }
                )
            }
        );
    }
    
    /**
     * Attaches a file to a Jira issue
     */
    public boolean attachFile(String issueKey, String filePath, String fileName) {
        if (!isConfigured()) {
            return false;
        }
        
        try {
            Response response = RestAssured.given()
                    .baseUri(baseUrl)
                    .header("Authorization", authHeader)
                    .header("X-Atlassian-Token", "no-check")
                    .contentType("multipart/form-data")
                    .multiPart("file", new java.io.File(filePath), "application/octet-stream")
                    .post("/rest/api/3/issue/" + issueKey + "/attachments");
            
            return response.getStatusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error attaching file: " + e.getMessage());
            return false;
        }
    }
}
