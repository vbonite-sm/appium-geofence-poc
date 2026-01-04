package com.geofence.integrations.jira;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * CI/CD integration utility for automatically creating Jira defects from test failures.
 * Called by Jenkins pipeline when tests fail.
 */
public class JiraDefectCreator {

    private static final Logger logger = LoggerFactory.getLogger(JiraDefectCreator.class);

    private final JiraClient jiraClient;
    private final String projectKey;
    private String buildNumber;
    private String buildUrl;

    public JiraDefectCreator() {
        Properties props = loadProperties();
        
        String baseUrl = getConfigValue(props, "jira.base.url", "JIRA_BASE_URL");
        String email = getConfigValue(props, "jira.email", "JIRA_EMAIL");
        String apiToken = getConfigValue(props, "jira.api.token", "JIRA_API_TOKEN");
        this.projectKey = getConfigValue(props, "jira.project.key", "JIRA_PROJECT_KEY");
        
        this.jiraClient = new JiraClient(baseUrl, email, apiToken);
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("jira-config.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            logger.warn("Could not load jira-config.properties: {}", e.getMessage());
        }
        return props;
    }

    private String getConfigValue(Properties props, String propKey, String envKey) {
        String envValue = System.getenv(envKey);
        return envValue != null ? envValue : props.getProperty(propKey);
    }

    public void setBuildInfo(String buildNumber, String buildUrl) {
        this.buildNumber = buildNumber;
        this.buildUrl = buildUrl;
    }

    /**
     * Parses JUnit XML test results and creates Jira defects for failures
     */
    public List<String> createDefectsFromTestResults(String resultsDir) {
        List<String> createdIssues = new ArrayList<>();
        File dir = new File(resultsDir);
        
        if (!dir.exists() || !dir.isDirectory()) {
            logger.error("Results directory not found: {}", resultsDir);
            return createdIssues;
        }

        File[] xmlFiles = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (xmlFiles == null) {
            return createdIssues;
        }

        for (File xmlFile : xmlFiles) {
            try {
                List<TestFailure> failures = parseTestResults(xmlFile);
                for (TestFailure failure : failures) {
                    String issueKey = createDefectForFailure(failure);
                    if (issueKey != null) {
                        createdIssues.add(issueKey);
                        logger.info("Created Jira defect {} for test: {}", issueKey, failure.testName);
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing file {}: {}", xmlFile.getName(), e.getMessage());
            }
        }

        return createdIssues;
    }

    private List<TestFailure> parseTestResults(File xmlFile) throws Exception {
        List<TestFailure> failures = new ArrayList<>();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new FileInputStream(xmlFile));
        
        NodeList testcases = doc.getElementsByTagName("testcase");
        
        for (int i = 0; i < testcases.getLength(); i++) {
            Element testcase = (Element) testcases.item(i);
            NodeList failureNodes = testcase.getElementsByTagName("failure");
            NodeList errorNodes = testcase.getElementsByTagName("error");
            
            if (failureNodes.getLength() > 0 || errorNodes.getLength() > 0) {
                TestFailure failure = new TestFailure();
                failure.className = testcase.getAttribute("classname");
                failure.testName = testcase.getAttribute("name");
                failure.time = testcase.getAttribute("time");
                
                if (failureNodes.getLength() > 0) {
                    Element failureEl = (Element) failureNodes.item(0);
                    failure.message = failureEl.getAttribute("message");
                    failure.type = failureEl.getAttribute("type");
                    failure.stackTrace = failureEl.getTextContent();
                } else {
                    Element errorEl = (Element) errorNodes.item(0);
                    failure.message = errorEl.getAttribute("message");
                    failure.type = errorEl.getAttribute("type");
                    failure.stackTrace = errorEl.getTextContent();
                }
                
                failures.add(failure);
            }
        }
        
        return failures;
    }

    private String createDefectForFailure(TestFailure failure) {
        String summary = String.format("[Automation] Test Failure: %s", failure.testName);
        
        StringBuilder description = new StringBuilder();
        description.append("h2. Automated Test Failure\n\n");
        description.append("*Test Class:* ").append(failure.className).append("\n");
        description.append("*Test Method:* ").append(failure.testName).append("\n");
        description.append("*Execution Time:* ").append(failure.time).append("s\n");
        
        if (buildNumber != null) {
            description.append("*Build Number:* ").append(buildNumber).append("\n");
        }
        if (buildUrl != null) {
            description.append("*Build URL:* [View Build|").append(buildUrl).append("]\n");
        }
        
        description.append("\nh3. Error Details\n");
        description.append("*Error Type:* ").append(failure.type).append("\n");
        description.append("*Error Message:* ").append(failure.message).append("\n");
        
        description.append("\nh3. Stack Trace\n");
        description.append("{code:java}\n");
        // Truncate stack trace if too long
        String stackTrace = failure.stackTrace;
        if (stackTrace != null && stackTrace.length() > 5000) {
            stackTrace = stackTrace.substring(0, 5000) + "\n... (truncated)";
        }
        description.append(stackTrace != null ? stackTrace : "No stack trace available");
        description.append("\n{code}\n");

        // Add labels for filtering
        List<String> labels = List.of("automation", "test-failure", "ci-generated");
        
        return jiraClient.createIssue(projectKey, "Bug", summary, description.toString(), labels);
    }

    public static void main(String[] args) {
        JiraDefectCreator creator = new JiraDefectCreator();
        
        String buildNumber = null;
        String buildUrl = null;
        String resultsDir = "target/surefire-reports";
        
        // Parse command line arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--build-number=")) {
                buildNumber = args[i].substring("--build-number=".length());
            } else if (args[i].startsWith("--build-url=")) {
                buildUrl = args[i].substring("--build-url=".length());
            } else if (args[i].startsWith("--results-dir=")) {
                resultsDir = args[i].substring("--results-dir=".length());
            }
        }
        
        creator.setBuildInfo(buildNumber, buildUrl);
        
        logger.info("Creating Jira defects from test results in: {}", resultsDir);
        List<String> createdIssues = creator.createDefectsFromTestResults(resultsDir);
        
        if (createdIssues.isEmpty()) {
            logger.info("No test failures found or no defects created");
        } else {
            logger.info("Created {} Jira defects: {}", createdIssues.size(), createdIssues);
        }
    }

    private static class TestFailure {
        String className;
        String testName;
        String time;
        String message;
        String type;
        String stackTrace;
    }
}
