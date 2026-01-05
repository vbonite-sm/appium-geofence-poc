package com.geofence.integrations.confluence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * CI/CD integration utility for publishing test reports to Confluence.
 * Called by Jenkins pipeline after test execution.
 */
public class ConfluenceReportPublisher {

    private static final Logger logger = LoggerFactory.getLogger(ConfluenceReportPublisher.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ConfluenceClient confluenceClient;
    private String buildNumber;
    private String buildUrl;
    private String allureUrl;

    public ConfluenceReportPublisher() {
        Properties props = loadProperties();
        
        String baseUrl = getConfigValue(props, "confluence.base.url", "CONFLUENCE_BASE_URL");
        String email = getConfigValue(props, "jira.email", "JIRA_EMAIL");
        String apiToken = getConfigValue(props, "jira.api.token", "JIRA_API_TOKEN");
        String spaceKey = getConfigValue(props, "confluence.space.key", "CONFLUENCE_SPACE_KEY");
        
        // Fallback to Jira URL if Confluence URL not set
        if (baseUrl == null || baseUrl.isEmpty()) {
            String jiraUrl = getConfigValue(props, "jira.base.url", "JIRA_BASE_URL");
            if (jiraUrl != null) {
                baseUrl = jiraUrl.endsWith("/") ? jiraUrl + "wiki" : jiraUrl + "/wiki";
            }
        }
        
        this.confluenceClient = new ConfluenceClient(baseUrl, email, apiToken, spaceKey);
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

    public void setBuildInfo(String buildNumber, String buildUrl, String allureUrl) {
        this.buildNumber = buildNumber;
        this.buildUrl = buildUrl;
        this.allureUrl = allureUrl;
    }

    /**
     * Parses test results and publishes a summary report to Confluence
     */
    public String publishTestReport(String resultsDir) {
        TestSummary summary = aggregateTestResults(resultsDir);
        String content = generateReportContent(summary);
        
        String title = String.format("Test Report - Build #%s - %s", 
            buildNumber != null ? buildNumber : "Local",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        String pageId = confluenceClient.createPage(title, content, null);
        
        if (pageId != null) {
            logger.info("Published test report to Confluence: {}", pageId);
        } else {
            logger.error("Failed to publish test report to Confluence");
        }
        
        return pageId;
    }

    private TestSummary aggregateTestResults(String resultsDir) {
        TestSummary summary = new TestSummary();
        File dir = new File(resultsDir);
        
        if (!dir.exists() || !dir.isDirectory()) {
            logger.warn("Results directory not found: {}", resultsDir);
            return summary;
        }

        // First try to parse Allure JSON results (most accurate for multi-stage builds)
        File allureDir = new File(dir.getParentFile(), "allure-results");
        if (allureDir.exists() && allureDir.isDirectory()) {
            try {
                parseAllureResults(allureDir, summary);
                if (summary.totalTests > 0) {
                    logger.info("Parsed Allure results: {} total, {} passed, {} failed, {} skipped",
                        summary.totalTests, summary.passed, summary.failures, summary.skipped);
                    return summary;
                }
            } catch (Exception e) {
                logger.warn("Failed to parse Allure results: {}", e.getMessage());
            }
        }

        // Fallback to testng-results.xml (TestNG native format)
        File testngResults = new File(dir, "testng-results.xml");
        if (testngResults.exists()) {
            try {
                parseTestNGResults(testngResults, summary);
                logger.info("Parsed TestNG results: {} total, {} passed, {} failed, {} skipped",
                    summary.totalTests, summary.passed, summary.failures, summary.skipped);
                return summary;
            } catch (Exception e) {
                logger.warn("Failed to parse testng-results.xml: {}", e.getMessage());
            }
        }

        // Fallback to JUnit/Surefire XML format (TEST-*.xml files)
        File[] xmlFiles = dir.listFiles((d, name) -> name.startsWith("TEST-") && name.endsWith(".xml"));
        
        if (xmlFiles == null || xmlFiles.length == 0) {
            // Try any XML files that might be surefire reports
            xmlFiles = dir.listFiles((d, name) -> name.endsWith(".xml") && !name.equals("testng-results.xml") && !name.equals("testng-failed.xml"));
        }
        
        if (xmlFiles == null || xmlFiles.length == 0) {
            logger.warn("No test result XML files found in: {}", resultsDir);
            return summary;
        }

        for (File xmlFile : xmlFiles) {
            try {
                parseTestSuite(xmlFile, summary);
            } catch (Exception e) {
                logger.error("Error processing file {}: {}", xmlFile.getName(), e.getMessage());
            }
        }

        return summary;
    }

    /**
     * Parses Allure JSON result files to get accurate test counts
     */
    private void parseAllureResults(File allureDir, TestSummary summary) throws Exception {
        File[] resultFiles = allureDir.listFiles((d, name) -> name.endsWith("-result.json"));
        
        if (resultFiles == null || resultFiles.length == 0) {
            logger.warn("No Allure result files found in: {}", allureDir.getPath());
            return;
        }
        
        java.util.Map<String, SuiteResult> suiteMap = new java.util.HashMap<>();
        
        for (File resultFile : resultFiles) {
            try {
                JsonNode result = objectMapper.readTree(resultFile);
                String status = result.has("status") ? result.get("status").asText() : "unknown";
                long duration = 0;
                if (result.has("stop") && result.has("start")) {
                    duration = result.get("stop").asLong() - result.get("start").asLong();
                }
                
                summary.totalTests++;
                summary.totalTime += duration / 1000.0; // Convert ms to seconds
                
                switch (status.toLowerCase()) {
                    case "passed":
                        summary.passed++;
                        break;
                    case "failed":
                        summary.failures++;
                        break;
                    case "broken":
                        summary.errors++;
                        break;
                    case "skipped":
                        summary.skipped++;
                        break;
                }
                
                // Extract suite name from labels
                String suiteName = "Default Suite";
                if (result.has("labels")) {
                    for (JsonNode label : result.get("labels")) {
                        if ("suite".equals(label.get("name").asText())) {
                            suiteName = label.get("value").asText();
                            break;
                        }
                    }
                }
                
                // Aggregate by suite
                SuiteResult suiteResult = suiteMap.computeIfAbsent(suiteName, 
                    name -> new SuiteResult(name, 0, 0, 0, 0.0));
                suiteResult.tests++;
                suiteResult.time += duration / 1000.0;
                if ("failed".equalsIgnoreCase(status) || "broken".equalsIgnoreCase(status)) {
                    suiteResult.failed++;
                } else if ("skipped".equalsIgnoreCase(status)) {
                    suiteResult.skipped++;
                }
                
            } catch (Exception e) {
                logger.debug("Error parsing Allure result file {}: {}", resultFile.getName(), e.getMessage());
            }
        }
        
        summary.suites.addAll(suiteMap.values());
    }

    /**
     * Parses TestNG native XML format (testng-results.xml)
     */
    private void parseTestNGResults(File xmlFile, TestSummary summary) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new FileInputStream(xmlFile));
        
        // Parse root <testng-results> element which has the totals
        NodeList resultsList = doc.getElementsByTagName("testng-results");
        if (resultsList.getLength() > 0) {
            Element results = (Element) resultsList.item(0);
            
            summary.totalTests = parseIntAttribute(results, "total", 0);
            summary.passed = parseIntAttribute(results, "passed", 0);
            summary.failures = parseIntAttribute(results, "failed", 0);
            summary.skipped = parseIntAttribute(results, "skipped", 0);
            summary.errors = 0; // TestNG doesn't distinguish errors from failures
        }
        
        // Parse suite information for breakdown
        NodeList suites = doc.getElementsByTagName("suite");
        for (int i = 0; i < suites.getLength(); i++) {
            Element suite = (Element) suites.item(i);
            String suiteName = suite.getAttribute("name");
            String durationStr = suite.getAttribute("duration-ms");
            double duration = 0.0;
            try {
                duration = durationStr != null && !durationStr.isEmpty() 
                    ? Double.parseDouble(durationStr) / 1000.0 : 0.0;
            } catch (NumberFormatException e) {
                // ignore
            }
            
            // Count tests in this suite
            int suiteTests = 0, suiteFailed = 0, suiteSkipped = 0;
            NodeList testMethods = suite.getElementsByTagName("test-method");
            for (int j = 0; j < testMethods.getLength(); j++) {
                Element method = (Element) testMethods.item(j);
                // Skip config methods
                String isConfig = method.getAttribute("is-config");
                if ("true".equals(isConfig)) {
                    continue;
                }
                
                suiteTests++;
                String status = method.getAttribute("status");
                if ("FAIL".equals(status)) {
                    suiteFailed++;
                } else if ("SKIP".equals(status)) {
                    suiteSkipped++;
                }
            }
            
            if (suiteName != null && !suiteName.isEmpty() && suiteTests > 0) {
                summary.suites.add(new SuiteResult(suiteName, suiteTests, suiteFailed, suiteSkipped, duration));
            }
            
            summary.totalTime += duration;
        }
    }

    private void parseTestSuite(File xmlFile, TestSummary summary) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new FileInputStream(xmlFile));
        
        NodeList suites = doc.getElementsByTagName("testsuite");
        
        for (int i = 0; i < suites.getLength(); i++) {
            Element suite = (Element) suites.item(i);
            
            int tests = parseIntAttribute(suite, "tests", 0);
            int failures = parseIntAttribute(suite, "failures", 0);
            int errors = parseIntAttribute(suite, "errors", 0);
            int skipped = parseIntAttribute(suite, "skipped", 0);
            double time = parseDoubleAttribute(suite, "time", 0.0);
            
            summary.totalTests += tests;
            summary.failures += failures;
            summary.errors += errors;
            summary.skipped += skipped;
            summary.totalTime += time;
            
            String suiteName = suite.getAttribute("name");
            if (suiteName != null && !suiteName.isEmpty()) {
                summary.suites.add(new SuiteResult(suiteName, tests, failures + errors, skipped, time));
            }
        }
        
        summary.passed = summary.totalTests - summary.failures - summary.errors - summary.skipped;
    }

    private int parseIntAttribute(Element element, String attr, int defaultValue) {
        try {
            String value = element.getAttribute(attr);
            return value != null && !value.isEmpty() ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private double parseDoubleAttribute(Element element, String attr, double defaultValue) {
        try {
            String value = element.getAttribute(attr);
            return value != null && !value.isEmpty() ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String generateReportContent(TestSummary summary) {
        StringBuilder html = new StringBuilder();
        
        // Header
        html.append("<h1>🧪 Test Execution Report</h1>\n");
        
        // Build Info
        html.append("<h2>📋 Build Information</h2>\n");
        html.append("<table>\n");
        html.append("<tbody>\n");
        if (buildNumber != null) {
            html.append("<tr><td><strong>Build Number</strong></td><td>").append(buildNumber).append("</td></tr>\n");
        }
        html.append("<tr><td><strong>Execution Date</strong></td><td>")
            .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .append("</td></tr>\n");
        html.append("<tr><td><strong>Total Duration</strong></td><td>")
            .append(String.format("%.2f seconds", summary.totalTime))
            .append("</td></tr>\n");
        if (buildUrl != null) {
            html.append("<tr><td><strong>Jenkins Build</strong></td><td><a href=\"")
                .append(buildUrl).append("\">View Build</a></td></tr>\n");
        }
        if (allureUrl != null) {
            html.append("<tr><td><strong>Allure Report</strong></td><td><a href=\"")
                .append(allureUrl).append("\">View Detailed Report</a></td></tr>\n");
        }
        html.append("</tbody>\n</table>\n");
        
        // Summary Panel
        double passRate = summary.totalTests > 0 
            ? (double) summary.passed / summary.totalTests * 100 : 0;
        String statusColor = passRate == 100 ? "#00875A" : passRate >= 80 ? "#FF991F" : "#DE350B";
        String statusIcon = passRate == 100 ? "✅" : passRate >= 80 ? "⚠️" : "❌";
        
        html.append("<h2>📊 Results Summary</h2>\n");
        html.append("<ac:structured-macro ac:name=\"panel\">\n");
        html.append("<ac:parameter ac:name=\"borderColor\">").append(statusColor).append("</ac:parameter>\n");
        html.append("<ac:rich-text-body>\n");
        html.append("<p style=\"font-size: 24px; color: ").append(statusColor).append(";\">")
            .append(statusIcon).append(" Pass Rate: ")
            .append(String.format("%.1f%%", passRate))
            .append("</p>\n");
        html.append("</ac:rich-text-body>\n");
        html.append("</ac:structured-macro>\n");
        
        // Results Table
        html.append("<table>\n");
        html.append("<thead><tr><th>Status</th><th>Count</th></tr></thead>\n");
        html.append("<tbody>\n");
        html.append("<tr><td style=\"color: #00875A;\">✅ Passed</td><td><strong>")
            .append(summary.passed).append("</strong></td></tr>\n");
        html.append("<tr><td style=\"color: #DE350B;\">❌ Failed</td><td><strong>")
            .append(summary.failures + summary.errors).append("</strong></td></tr>\n");
        html.append("<tr><td style=\"color: #6B778C;\">⏭️ Skipped</td><td><strong>")
            .append(summary.skipped).append("</strong></td></tr>\n");
        html.append("<tr><td><strong>Total</strong></td><td><strong>")
            .append(summary.totalTests).append("</strong></td></tr>\n");
        html.append("</tbody>\n</table>\n");
        
        // Suite Breakdown
        if (!summary.suites.isEmpty()) {
            html.append("<h2>📁 Test Suite Breakdown</h2>\n");
            html.append("<table>\n");
            html.append("<thead><tr><th>Suite</th><th>Tests</th><th>Failed</th><th>Skipped</th><th>Duration</th></tr></thead>\n");
            html.append("<tbody>\n");
            
            for (SuiteResult suite : summary.suites) {
                String rowColor = suite.failed > 0 ? "#FFEBE6" : "#E3FCEF";
                html.append("<tr style=\"background-color: ").append(rowColor).append(";\">")
                    .append("<td>").append(suite.name).append("</td>")
                    .append("<td>").append(suite.tests).append("</td>")
                    .append("<td>").append(suite.failed).append("</td>")
                    .append("<td>").append(suite.skipped).append("</td>")
                    .append("<td>").append(String.format("%.2fs", suite.time)).append("</td>")
                    .append("</tr>\n");
            }
            
            html.append("</tbody>\n</table>\n");
        }
        
        // Footer
        html.append("<hr/>\n");
        html.append("<p><em>This report was automatically generated by the Geofence Automation Framework.</em></p>\n");
        
        return html.toString();
    }

    public static void main(String[] args) {
        ConfluenceReportPublisher publisher = new ConfluenceReportPublisher();
        
        String buildNumber = null;
        String buildUrl = null;
        String allureUrl = null;
        String resultsDir = "target/surefire-reports";
        
        // Parse command line arguments
        for (String arg : args) {
            if (arg.startsWith("--build-number=")) {
                buildNumber = arg.substring("--build-number=".length());
            } else if (arg.startsWith("--build-url=")) {
                buildUrl = arg.substring("--build-url=".length());
            } else if (arg.startsWith("--allure-url=")) {
                allureUrl = arg.substring("--allure-url=".length());
            } else if (arg.startsWith("--results-dir=")) {
                resultsDir = arg.substring("--results-dir=".length());
            }
        }
        
        publisher.setBuildInfo(buildNumber, buildUrl, allureUrl);
        
        logger.info("Publishing test report from: {}", resultsDir);
        String pageId = publisher.publishTestReport(resultsDir);
        
        if (pageId != null) {
            logger.info("Successfully published report to Confluence. Page ID: {}", pageId);
        } else {
            logger.error("Failed to publish report to Confluence");
            System.exit(1);
        }
    }

    private static class TestSummary {
        int totalTests = 0;
        int passed = 0;
        int failures = 0;
        int errors = 0;
        int skipped = 0;
        double totalTime = 0.0;
        java.util.List<SuiteResult> suites = new java.util.ArrayList<>();
    }

    private static class SuiteResult {
        String name;
        int tests;
        int failed;
        int skipped;
        double time;

        SuiteResult(String name, int tests, int failed, int skipped, double time) {
            this.name = name;
            this.tests = tests;
            this.failed = failed;
            this.skipped = skipped;
            this.time = time;
        }
    }
}
