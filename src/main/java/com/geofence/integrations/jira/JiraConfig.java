package com.geofence.integrations.jira;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration class for Jira integration.
 * Loads credentials from environment variables or properties file.
 * Uses Singleton pattern for consistent configuration access across the application.
 */
@SuppressWarnings("java:S6548") // Singleton pattern is intentional for configuration management
public class JiraConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(JiraConfig.class);
    
    private static JiraConfig instance;
    private final Properties properties;
    
    // Jira configuration keys
    public static final String JIRA_BASE_URL = "jira.base.url";
    public static final String JIRA_EMAIL = "jira.email";
    public static final String JIRA_API_TOKEN = "jira.api.token";
    public static final String JIRA_PROJECT_KEY = "jira.project.key";
    public static final String JIRA_ENABLED = "jira.enabled";
    
    private JiraConfig() {
        properties = new Properties();
        loadConfiguration();
    }
    
    public static synchronized JiraConfig getInstance() {
        if (instance == null) {
            instance = new JiraConfig();
        }
        return instance;
    }
    
    private void loadConfiguration() {
        // First, try to load from properties file
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("jira-config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            logger.info("jira-config.properties not found, using environment variables");
        }
        
        // Override with environment variables if present
        loadFromEnvironment();
    }
    
    private void loadFromEnvironment() {
        String baseUrl = System.getenv("JIRA_BASE_URL");
        if (baseUrl != null) {
            properties.setProperty(JIRA_BASE_URL, baseUrl);
        }
        
        String email = System.getenv("JIRA_EMAIL");
        if (email != null) {
            properties.setProperty(JIRA_EMAIL, email);
        }
        
        String apiToken = System.getenv("JIRA_API_TOKEN");
        if (apiToken != null) {
            properties.setProperty(JIRA_API_TOKEN, apiToken);
        }
        
        String projectKey = System.getenv("JIRA_PROJECT_KEY");
        if (projectKey != null) {
            properties.setProperty(JIRA_PROJECT_KEY, projectKey);
        }
        
        String enabled = System.getenv("JIRA_ENABLED");
        if (enabled != null) {
            properties.setProperty(JIRA_ENABLED, enabled);
        }
    }
    
    public String getBaseUrl() {
        return properties.getProperty(JIRA_BASE_URL, "");
    }
    
    public String getEmail() {
        return properties.getProperty(JIRA_EMAIL, "");
    }
    
    public String getApiToken() {
        return properties.getProperty(JIRA_API_TOKEN, "");
    }
    
    public String getProjectKey() {
        return properties.getProperty(JIRA_PROJECT_KEY, "GEO");
    }
    
    public boolean isEnabled() {
        return Boolean.parseBoolean(properties.getProperty(JIRA_ENABLED, "true"));
    }
    
    public boolean isConfigured() {
        return !getBaseUrl().isEmpty() 
            && !getEmail().isEmpty() 
            && !getApiToken().isEmpty();
    }
}
