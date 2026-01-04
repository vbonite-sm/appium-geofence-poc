package com.geofence.integrations.jira;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * TestNG listener that automatically creates Jira defects for failed tests.
 * Can be enabled/disabled via configuration.
 */
public class JiraTestListener implements ITestListener {
    
    private static final Logger logger = LoggerFactory.getLogger(JiraTestListener.class);
    
    private final JiraClient jiraClient;
    private final JiraConfig config;
    private final List<String> createdIssues;
    
    public JiraTestListener() {
        this.jiraClient = new JiraClient();
        this.config = JiraConfig.getInstance();
        this.createdIssues = new ArrayList<>();
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        if (logger.isInfoEnabled()) {
            logger.info("[Jira] Test started: {}", getTestName(result));
        }
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        if (logger.isInfoEnabled()) {
            logger.info("[Jira] Test passed: {}", getTestName(result));
        }
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        if (!config.isEnabled() || !config.isConfigured()) {
            logger.info("[Jira] Integration disabled or not configured. Skipping defect creation.");
            return;
        }
        
        String testName = getTestName(result);
        String errorMessage = getErrorMessage(result);
        String stackTrace = getStackTrace(result);
        
        logger.info("[Jira] Test failed: {}", testName);
        logger.info("[Jira] Creating defect in Jira...");
        
        String issueKey = jiraClient.createDefect(testName, errorMessage, stackTrace);
        
        if (issueKey != null) {
            createdIssues.add(issueKey);
            logger.info("[Jira] Defect created: {}", issueKey);
            
            // Add additional context as comment
            String additionalInfo = buildAdditionalInfo(result);
            jiraClient.addComment(issueKey, additionalInfo);
        }
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        if (logger.isInfoEnabled()) {
            logger.info("[Jira] Test skipped: {}", getTestName(result));
        }
    }
    
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // Handle partial failures if needed
    }
    
    @Override
    public void onStart(ITestContext context) {
        logger.info("[Jira] Test suite started: {}", context.getName());
    }
    
    @Override
    public void onFinish(ITestContext context) {
        logger.info("[Jira] Test suite finished: {}", context.getName());
        logger.info("[Jira] Passed: {}", context.getPassedTests().size());
        logger.info("[Jira] Failed: {}", context.getFailedTests().size());
        logger.info("[Jira] Skipped: {}", context.getSkippedTests().size());
        
        if (!createdIssues.isEmpty() && logger.isInfoEnabled()) {
            logger.info("[Jira] Created issues during this run: {}", String.join(", ", createdIssues));
        }
    }
    
    private String getTestName(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    }
    
    private String getErrorMessage(ITestResult result) {
        Throwable throwable = result.getThrowable();
        return throwable != null ? throwable.getMessage() : "Unknown error";
    }
    
    private String getStackTrace(ITestResult result) {
        Throwable throwable = result.getThrowable();
        if (throwable == null) {
            return "No stack trace available";
        }
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        
        String stackTrace = sw.toString();
        // Limit stack trace length for Jira
        if (stackTrace.length() > 5000) {
            stackTrace = stackTrace.substring(0, 5000) + "\n... (truncated)";
        }
        
        return stackTrace;
    }
    
    private String buildAdditionalInfo(ITestResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("h3. Additional Test Information\n\n");
        sb.append("*Test Class:* ").append(result.getTestClass().getName()).append("\n");
        sb.append("*Test Method:* ").append(result.getMethod().getMethodName()).append("\n");
        sb.append("*Duration:* ").append((result.getEndMillis() - result.getStartMillis())).append(" ms\n");
        
        // Add test parameters if any
        Object[] params = result.getParameters();
        if (params != null && params.length > 0) {
            sb.append("*Parameters:*\n");
            for (int i = 0; i < params.length; i++) {
                sb.append("  - Param ").append(i + 1).append(": ").append(params[i]).append("\n");
            }
        }
        
        // Add groups
        String[] groups = result.getMethod().getGroups();
        if (groups != null && groups.length > 0) {
            sb.append("*Groups:* ").append(String.join(", ", groups)).append("\n");
        }
        
        return sb.toString();
    }
    
    public List<String> getCreatedIssues() {
        return new ArrayList<>(createdIssues);
    }
}
