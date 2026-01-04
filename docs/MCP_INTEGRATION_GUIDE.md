# Atlassian MCP (Model Context Protocol) Integration Guide

## Overview

This guide explains how to integrate the automation framework with Atlassian MCP for AI-powered interactions with Jira and Confluence through GitHub Copilot or other AI assistants.

---

## What is Atlassian MCP?

MCP (Model Context Protocol) enables AI assistants to interact with external tools and services. The Atlassian MCP server allows AI to:

- Create, update, and search Jira issues
- Read and create Confluence pages
- Manage project workflows
- Automate documentation

---

## Prerequisites

- Node.js 18+ installed
- npm or npx available
- Atlassian account with API token
- VS Code with GitHub Copilot or compatible AI extension

---

## Setup Instructions

### Step 1: Install MCP Support

For VS Code, ensure you have a compatible extension that supports MCP:

```bash
# GitHub Copilot Chat extension supports MCP
code --install-extension GitHub.copilot-chat
```

### Step 2: Configure MCP Settings

#### Option A: VS Code Settings

Create/update `.vscode/mcp-settings.json`:

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

#### Option B: Global MCP Configuration

Create `~/.config/mcp/config.json` (Linux/macOS) or `%APPDATA%\mcp\config.json` (Windows):

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

### Step 3: Verify Installation

Test the MCP server directly:

```bash
npx -y @anthropic/mcp-server-atlassian
```

---

## Using Atlassian MCP with AI Copilot

### Natural Language Commands

Once configured, you can use natural language to interact with Jira/Confluence:

#### Jira Commands

```
"Create a new bug in project GEO titled 'Geofence notification not received'"

"Search for all open defects in the GEO project"

"Add a comment to GEO-123 saying 'Fixed in commit abc123'"

"Transition GEO-123 to Done status"

"List all issues assigned to me in GEO project"

"Create a task to implement iOS geofence tests"
```

#### Confluence Commands

```
"Create a new page in GEO space titled 'Test Execution Report'"

"Update the test documentation page with latest results"

"Search for pages containing 'geofence' in the GEO space"

"Get content from the API documentation page"
```

### Example Conversations

**Creating a Defect:**
```
User: Create a Jira bug for a test failure where the geofence exit notification was not received on iOS

AI: I'll create that bug for you in the GEO project.
[Creates issue GEO-45]
Created bug GEO-45: "[iOS] Geofence exit notification not received"
```

**Adding Test Results:**
```
User: Add a comment to GEO-45 with today's test results: 4 passed, 1 failed

AI: I'll add that comment to GEO-45.
[Adds comment with test results summary]
Comment added successfully.
```

---

## IntelliJ IDEA Integration

### Option 1: Atlassian Plugin

1. **Install Atlassian Plugin:**
   - Settings → Plugins → Marketplace
   - Search "Atlassian"
   - Install and restart

2. **Configure Connection:**
   - View → Tool Windows → Atlassian
   - Add Server Connection
   - Enter Jira URL and credentials

3. **Features:**
   - View Jira issues in IDE
   - Create issues from code
   - Link commits to issues

### Option 2: JetBrains AI Assistant with MCP

If using JetBrains AI Assistant:

1. Configure MCP in `~/.config/jetbrains/mcp.json`
2. Enable MCP integration in AI settings
3. Use natural language commands

---

## Security Configuration

### Environment Variables (Recommended)

Set environment variables instead of hardcoding:

**Windows PowerShell:**
```powershell
[System.Environment]::SetEnvironmentVariable('ATLASSIAN_HOST', 'your-site.atlassian.net', 'User')
[System.Environment]::SetEnvironmentVariable('ATLASSIAN_USER_EMAIL', 'your-email@example.com', 'User')
[System.Environment]::SetEnvironmentVariable('ATLASSIAN_API_TOKEN', 'your-token', 'User')
```

**Linux/macOS (.bashrc or .zshrc):**
```bash
export ATLASSIAN_HOST="your-site.atlassian.net"
export ATLASSIAN_USER_EMAIL="your-email@example.com"
export ATLASSIAN_API_TOKEN="your-token"
```

### Using Config with Environment Variables

```json
{
  "mcpServers": {
    "atlassian": {
      "command": "npx",
      "args": ["-y", "@anthropic/mcp-server-atlassian"],
      "env": {
        "ATLASSIAN_HOST": "${ATLASSIAN_HOST}",
        "ATLASSIAN_USER_EMAIL": "${ATLASSIAN_USER_EMAIL}",
        "ATLASSIAN_API_TOKEN": "${ATLASSIAN_API_TOKEN}"
      }
    }
  }
}
```

---

## Available MCP Tools

The Atlassian MCP server provides these tools:

### Jira Tools

| Tool | Description |
|------|-------------|
| `jira_create_issue` | Create a new issue |
| `jira_get_issue` | Get issue details |
| `jira_update_issue` | Update an existing issue |
| `jira_search` | Search issues using JQL |
| `jira_add_comment` | Add comment to an issue |
| `jira_transition` | Change issue status |
| `jira_get_transitions` | Get available transitions |

### Confluence Tools

| Tool | Description |
|------|-------------|
| `confluence_create_page` | Create a new page |
| `confluence_get_page` | Get page content |
| `confluence_update_page` | Update page content |
| `confluence_search` | Search pages using CQL |
| `confluence_get_space` | Get space information |

---

## Automation Workflows

### Post-Test Execution Workflow

1. **Test runs and fails**
2. **JiraTestListener creates defect automatically**
3. **Use AI to add details:**
   ```
   "Add stack trace from latest test failure to GEO-45"
   ```

### Documentation Workflow

1. **After test suite completes:**
   ```
   "Create a Confluence page with test results summary for today's run"
   ```

2. **Update existing documentation:**
   ```
   "Update the test coverage page to include the new geofence tests"
   ```

---

## Troubleshooting

### MCP Server Not Starting

```bash
# Check if npx works
npx --version

# Try running directly
npx -y @anthropic/mcp-server-atlassian --help
```

### Authentication Issues

```bash
# Test API token
curl -u email:token https://your-site.atlassian.net/rest/api/3/myself
```

### Connection Errors

1. Verify `ATLASSIAN_HOST` doesn't include `https://`
2. Check if VPN/firewall blocks connection
3. Ensure API token has correct permissions

---

## Best Practices

1. **Use service accounts** for automation
2. **Rotate API tokens** regularly
3. **Never commit tokens** to version control
4. **Use environment variables** for sensitive data
5. **Test with read-only operations** first

---

## Additional Resources

- [Atlassian REST API Documentation](https://developer.atlassian.com/cloud/jira/platform/rest/v3/)
- [MCP Specification](https://modelcontextprotocol.io/)
- [GitHub Copilot Documentation](https://docs.github.com/en/copilot)
