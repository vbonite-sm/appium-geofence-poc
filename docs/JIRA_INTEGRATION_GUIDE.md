# Jira & Confluence Integration Guide

## Complete Setup Instructions for Automation Framework Integration

---

## Part 1: Jira Account Setup

### Step 1: Create Atlassian Account

1. **Navigate to Atlassian:**
   ```
   https://www.atlassian.com/software/jira/free
   ```

2. **Click "Get it free"**

3. **Choose sign-up method:**
   - Email (recommended for work)
   - Google Account
   - Microsoft Account
   - Apple ID

4. **Complete registration:**
   - Enter your email address
   - Verify email via link
   - Set your password
   - Enter your full name

### Step 2: Create Jira Site

1. **Choose site name:**
   ```
   Site name: geofence-automation
   URL: https://geofence-automation.atlassian.net
   ```

2. **Select Jira Software** (for development projects)

3. **Choose template:**
   - Select **Kanban** (simpler for testing)
   - Or **Scrum** (if using sprints)

4. **Create initial project:**
   ```
   Project Name: Geofence Automation
   Project Key: GEO
   ```

### Step 3: Configure Issue Types

1. **Go to Project Settings → Issue Types**

2. **Ensure these types exist:**
   - Bug (for test failures/defects)
   - Task (for automation tasks)
   - Story (optional, for test scenarios)

### Step 4: Set Up Users & Permissions

1. **Go to Site Settings → User Management**
2. **Invite team members** if needed
3. **Assign project roles:**
   - Administrator
   - Developer
   - Reporter

---

## Part 2: Generate API Token

### Step 1: Access Security Settings

1. **Go to Atlassian Account:**
   ```
   https://id.atlassian.com/manage-profile/security/api-tokens
   ```

2. **Or navigate via:**
   - Click your avatar → Account Settings → Security → API tokens

### Step 2: Create New Token

1. **Click "Create API token"**

2. **Enter label:**
   ```
   Label: Geofence-Automation-Framework
   ```

3. **Click "Create"**

4. **IMPORTANT: Copy the token immediately!**
   - You won't be able to see it again
   - Store it securely (password manager recommended)

### Step 3: Note Your Credentials

Save these for configuration:
```properties
JIRA_BASE_URL=https://your-site.atlassian.net
JIRA_EMAIL=your-email@example.com
JIRA_API_TOKEN=your-generated-api-token
JIRA_PROJECT_KEY=GEO
```

---

## Part 3: Configure Framework Integration

### Option A: Environment Variables (Recommended for CI/CD)

**Windows (PowerShell):**
```powershell
$env:JIRA_BASE_URL = "https://your-site.atlassian.net"
$env:JIRA_EMAIL = "your-email@example.com"
$env:JIRA_API_TOKEN = "your-api-token"
$env:JIRA_PROJECT_KEY = "GEO"
$env:JIRA_ENABLED = "true"
```

**Windows (Command Prompt):**
```cmd
set JIRA_BASE_URL=https://your-site.atlassian.net
set JIRA_EMAIL=your-email@example.com
set JIRA_API_TOKEN=your-api-token
set JIRA_PROJECT_KEY=GEO
set JIRA_ENABLED=true
```

**Linux/macOS:**
```bash
export JIRA_BASE_URL="https://your-site.atlassian.net"
export JIRA_EMAIL="your-email@example.com"
export JIRA_API_TOKEN="your-api-token"
export JIRA_PROJECT_KEY="GEO"
export JIRA_ENABLED="true"
```

### Option B: Properties File (Local Development)

Edit `src/main/resources/jira-config.properties`:
```properties
jira.base.url=https://your-site.atlassian.net
jira.email=your-email@example.com
jira.api.token=your-api-token
jira.project.key=GEO
jira.enabled=true
```

⚠️ **Warning:** Never commit API tokens to version control!

---

## Part 4: Jenkins Integration

### Step 1: Install Required Plugins

In Jenkins → Manage Jenkins → Manage Plugins → Available:
- Credentials Binding Plugin
- Pipeline Maven Integration Plugin

### Step 2: Add Jenkins Credentials

1. Go to **Jenkins → Manage Jenkins → Manage Credentials**
2. Select appropriate domain
3. Click **"Add Credentials"**

Add these credentials:

| ID | Type | Description |
|---|---|---|
| `jira-base-url` | Secret text | Your Jira URL |
| `jira-email` | Secret text | Your Atlassian email |
| `jira-api-token` | Secret text | Your API token |

### Step 3: Jenkinsfile Configuration

The Jenkinsfile is pre-configured with:
```groovy
environment {
    JIRA_BASE_URL = credentials('jira-base-url')
    JIRA_EMAIL = credentials('jira-email')
    JIRA_API_TOKEN = credentials('jira-api-token')
    JIRA_PROJECT_KEY = 'GEO'
    JIRA_ENABLED = 'true'
}
```

---

## Part 5: Confluence Setup (Optional)

### Step 1: Enable Confluence

1. In your Atlassian site, go to **Administration → Products**
2. Enable **Confluence** (included in free tier)

### Step 2: Create Space

1. Go to Confluence → Create Space
2. **Space name:** Geofence Automation Docs
3. **Space key:** GEO

### Step 3: Configure Environment Variables

```bash
export CONFLUENCE_BASE_URL="https://your-site.atlassian.net"
export CONFLUENCE_SPACE_KEY="GEO"
# Uses same JIRA_EMAIL and JIRA_API_TOKEN
```

---

## Part 6: IntelliJ IDEA Integration

### Using Atlassian Plugin

1. **Install Plugin:**
   - File → Settings → Plugins → Marketplace
   - Search "Atlassian"
   - Install "Atlassian"

2. **Configure:**
   - View → Tool Windows → Atlassian
   - Click "Add Server"
   - Enter your credentials

### Using Jira MCP Server (AI Copilot)

For AI integration with GitHub Copilot:

1. **Install MCP Extension** in VS Code/IntelliJ

2. **Configure MCP Server:**
   ```json
   {
     "mcpServers": {
       "atlassian": {
         "command": "npx",
         "args": ["-y", "@anthropic/mcp-server-atlassian"],
         "env": {
           "ATLASSIAN_HOST": "your-site.atlassian.net",
           "ATLASSIAN_USER_EMAIL": "your-email@example.com",
           "ATLASSIAN_API_TOKEN": "your-api-token"
         }
       }
     }
   }
   ```

3. **Use natural language commands:**
   - "Create a Jira task for implementing geofence validation"
   - "Add a bug report for failed test case"
   - "Search for open defects in GEO project"

---

## Part 7: Running Tests

### Run Atlassian API Tests

```bash
# Run Jira/Confluence integration tests
mvn test -DsuiteXmlFile=src/test/resources/testng-atlassian.xml
```

### Run All Tests with Jira Integration

```bash
# Tests will auto-create defects for failures
mvn test -DsuiteXmlFile=src/test/resources/testng-browserstack.xml
```

### Disable Jira Integration Temporarily

```bash
# Set environment variable
export JIRA_ENABLED=false
mvn test
```

---

## Part 8: Usage Examples

### Create Issue Programmatically

```java
JiraClient jira = new JiraClient();

// Create a task
String taskKey = jira.createTask(
    "Implement geofence boundary detection",
    "Add logic to detect when user crosses geofence boundary"
);

// Create a bug
String bugKey = jira.createDefect(
    "testGeofenceExit",
    "Expected notification not received",
    "java.lang.AssertionError..."
);

// Add comment
jira.addComment(taskKey, "Started implementation");
```

### Create Confluence Page

```java
ConfluenceClient confluence = new ConfluenceClient(
    baseUrl, email, apiToken, "GEO"
);

// Create test report
String pageId = confluence.createTestReportPage(
    "Geofence E2E Tests",
    4,  // passed
    1,  // failed
    0,  // skipped
    "http://ci.example.com/allure-report"
);
```

---

## Troubleshooting

### Common Issues

| Issue | Solution |
|---|---|
| 401 Unauthorized | Check email and API token |
| 404 Not Found | Verify project key exists |
| 403 Forbidden | Check user permissions |
| Connection Refused | Verify base URL format |

### Verify Configuration

```java
JiraConfig config = JiraConfig.getInstance();
System.out.println("Configured: " + config.isConfigured());
System.out.println("Base URL: " + config.getBaseUrl());
System.out.println("Project: " + config.getProjectKey());
```

### Test API Token

```bash
curl -u your-email@example.com:your-api-token \
  https://your-site.atlassian.net/rest/api/3/myself
```

---

## Security Best Practices

1. **Never commit API tokens** to version control
2. **Use environment variables** in CI/CD
3. **Rotate tokens** periodically (every 90 days)
4. **Use service accounts** for automation
5. **Limit permissions** to required access only

---

## Quick Reference

### API Endpoints

| Action | Endpoint |
|---|---|
| Create Issue | `POST /rest/api/3/issue` |
| Get Issue | `GET /rest/api/3/issue/{key}` |
| Add Comment | `POST /rest/api/3/issue/{key}/comment` |
| Search | `GET /rest/api/3/search?jql={query}` |
| Transitions | `GET/POST /rest/api/3/issue/{key}/transitions` |

### JQL Examples

```
project = GEO AND status = Open
project = GEO AND type = Bug
project = GEO AND created >= -7d
assignee = currentUser() AND status != Done
```
