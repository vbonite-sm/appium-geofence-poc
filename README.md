# Appium Geofence POC

Mobile test automation framework for geofence functionality testing on Android and iOS platforms. Supports local Appium execution and BrowserStack cloud testing with Jenkins CI/CD integration, **Jira defect tracking**, and **Confluence test reporting**.

## Tech Stack

- **Java 21**
- **Appium Java Client 8.6.0** with Selenium 4.16.1
- **TestNG 7.10.2** - Test framework
- **REST Assured 5.4.0** - API testing
- **Allure 2.25.0** - Test reporting
- **BrowserStack SDK 1.15.0** - Cloud device testing
- **Jira REST API** - Automated defect creation
- **Confluence REST API** - Test report publishing

## Project Structure

```
src/
├── main/java/com/geofence/
│   ├── config/
│   │   └── EnvironmentConfig.java       # YAML/properties config loader
│   ├── driver/
│   │   ├── CapabilitiesBuilder.java     # Fluent builder for Appium capabilities
│   │   ├── DriverFactory.java           # Creates drivers based on platform/mode
│   │   ├── DriverManager.java           # Thread-safe driver management
│   │   ├── DriverProvider.java          # Interface for driver creation
│   │   ├── LocalDriverProvider.java     # Local Appium server driver
│   │   └── BrowserStackDriverProvider.java
│   ├── integrations/
│   │   ├── jira/
│   │   │   ├── JiraClient.java          # Jira REST API client
│   │   │   ├── JiraConfig.java          # Jira configuration loader
│   │   │   ├── JiraDefectCreator.java   # CI/CD defect creation utility
│   │   │   └── JiraTestListener.java    # Auto-create defects on test failure
│   │   └── confluence/
│   │       ├── ConfluenceClient.java    # Confluence REST API client
│   │       └── ConfluenceReportPublisher.java  # CI/CD report publishing
│   ├── models/
│   │   ├── GeoLocation.java             # Location model with distance calculation
│   │   ├── GeoFenceRequest.java         # API request model
│   │   ├── GeoFenceResponse.java        # API response model
│   │   ├── Platform.java                # ANDROID, IOS enum
│   │   └── ExecutionMode.java           # LOCAL, BROWSERSTACK enum
│   ├── pages/
│   │   ├── BasePage.java                # Page object base class
│   │   ├── GeofenceHomePage.java
│   │   ├── HomePage.java
│   │   └── AccessibilityPage.java
│   └── services/
│       ├── LocationService.java         # Location simulation operations
│       └── GeofenceService.java         # Geofence business logic
│
├── main/resources/
│   └── jira-config.properties.example   # Template for Jira/Confluence credentials
│
├── test/java/com/geofence/
│   ├── tests/
│   │   ├── base/
│   │   │   └── BaseTest.java            # Unified base with Template Method
│   │   ├── android/
│   │   │   ├── BaseTestAndroid.java     # Android-specific defaults
│   │   │   ├── GeofenceTest.java        # Android geofence tests
│   │   │   └── NavigationTest.java      # Android navigation tests
│   │   ├── ios/
│   │   │   ├── BaseTestiOS.java         # iOS-specific defaults
│   │   │   └── GeofenceTestiOS.java     # iOS geofence tests
│   │   └── api/
│   │       ├── GeofenceApiTest.java     # Geofence API tests
│   │       ├── JiraApiTests.java        # Jira integration tests
│   │       └── ConfluenceApiTests.java  # Confluence integration tests
│   ├── dataproviders/
│   │   └── GeofenceDataProvider.java    # TestNG data providers
│   ├── listeners/
│   │   ├── RetryAnalyzer.java           # Test retry mechanism
│   │   └── TestListener.java            # Logging and Allure integration
│   └── utils/
│       └── AllureUtils.java
│
└── test/resources/
    ├── config/
    │   ├── application.yaml             # Base configuration
    │   ├── application-local.yaml       # Local overrides
    │   ├── application-browserstack.yaml
    │   └── application-ci.yaml
    ├── testng-atlassian.xml             # Jira/Confluence test suite
    └── testdata/
        ├── locations.json               # Test location coordinates
        ├── geofence-scenarios.json      # Test scenarios
        └── api-payloads.json            # API test payloads
```

## Design Patterns

| Pattern | Implementation |
|---------|----------------|
| Factory | `DriverFactory` creates appropriate driver based on platform and execution mode |
| Builder | `CapabilitiesBuilder` provides fluent API for Appium capabilities |
| Strategy | `DriverProvider` interface with `LocalDriverProvider` and `BrowserStackDriverProvider` |
| Template Method | `BaseTest` with hooks (`onDriverInitialized`, `onBeforeDriverQuit`) |
| Page Object | All page classes extend `BasePage` |
| Service Layer | `LocationService`, `GeofenceService` encapsulate business logic |
| Data Provider | `GeofenceDataProvider` supplies test data to TestNG |
| AAA Pattern | All tests follow Arrange-Act-Assert structure with clear comments |

## Setup

### Prerequisites

- Java 21+
- Maven 3.8+
- Appium Server 2.x (for local execution)
- Android SDK with emulator or real device (for local Android testing)

### Installation

```bash
git clone <repository-url>
cd appium-geofence-poc
mvn clean install -DskipTests
```

### Environment Variables

For BrowserStack execution:

```bash
# Windows
set BROWSERSTACK_USERNAME=your_username
set BROWSERSTACK_ACCESSKEY=your_access_key

# Linux/macOS
export BROWSERSTACK_USERNAME=your_username
export BROWSERSTACK_ACCESSKEY=your_access_key
```

For Jira/Confluence integration:

```bash
# Windows
set JIRA_BASE_URL=https://your-site.atlassian.net
set JIRA_EMAIL=your-email@example.com
set JIRA_API_TOKEN=your_api_token
set JIRA_PROJECT_KEY=GEO
set CONFLUENCE_BASE_URL=https://your-site.atlassian.net/wiki
set CONFLUENCE_SPACE_KEY=GEO

# Linux/macOS
export JIRA_BASE_URL=https://your-site.atlassian.net
export JIRA_EMAIL=your-email@example.com
export JIRA_API_TOKEN=your_api_token
export JIRA_PROJECT_KEY=GEO
export CONFLUENCE_BASE_URL=https://your-site.atlassian.net/wiki
export CONFLUENCE_SPACE_KEY=GEO
```

Alternatively, copy `src/main/resources/jira-config.properties.example` to `jira-config.properties` and fill in your credentials.

## Configuration

Configuration uses YAML files with environment-specific overrides. The system loads:
1. `application.yaml` (base)
2. `application-{profile}.yaml` (profile-specific)
3. Environment variables (highest priority)

Set the active profile:
```bash
mvn test -Dprofile=browserstack
```

### Key Configuration Properties

| Property | Description |
|----------|-------------|
| `appium.server.url` | Local Appium server URL |
| `app.local.path` | Path to local APK/IPA |
| `browserstack.app` | BrowserStack app URL |
| `browserstack.device` | Target device name |
| `test.implicit.wait` | Implicit wait timeout (seconds) |

## Running Tests

### Local Android Execution

Start Appium server, then:

```bash
mvn clean test -Dtest=GeofenceTest -DexecutionMode=local -Dplatform=android
```

### BrowserStack Execution

```bash
# Android
mvn clean test -Dtest=GeofenceTest -DexecutionMode=browserstack -Dplatform=android

# iOS
mvn clean test -Dtest=GeofenceTestiOS -DexecutionMode=browserstack -Dplatform=ios
```

### Run All Tests

```bash
mvn clean test
```

### Run with Allure Report

```bash
mvn clean test allure:serve
```

## TestNG Parameters

Tests accept the following parameters via TestNG XML or system properties:

| Parameter | Values | Default |
|-----------|--------|---------|
| `executionMode` | `local`, `browserstack` | `local` |
| `platform` | `android`, `ios` | `android` |

Example TestNG XML:
```xml
<suite name="Geofence Suite">
    <test name="Android Tests">
        <parameter name="executionMode" value="browserstack"/>
        <parameter name="platform" value="android"/>
        <classes>
            <class name="com.geofence.tests.GeofenceTest"/>
        </classes>
    </test>
</suite>
```

## API Tests

```bash
# Run all API tests
mvn clean test -Dtest=GeofenceApiTest

# Run specific API test method
mvn clean test -Dtest=GeofenceApiTest#testCreateGeoFence

# Run API tests with verbose logging
mvn clean test -Dtest=GeofenceApiTest -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
```

API tests use REST Assured and validate geofence API endpoints.

## Jira & Confluence Integration

### Run Atlassian API Tests

```bash
# Run Jira integration tests
mvn clean test -Dtest=JiraApiTests

# Run Confluence integration tests
mvn clean test -Dtest=ConfluenceApiTests

# Run all Atlassian tests
mvn clean test -DsuiteXmlFile=src/test/resources/testng-atlassian.xml
```

### Auto-Create Jira Defects on Test Failure

Add `JiraTestListener` to your test to automatically create Jira bugs when tests fail:

```java
@Listeners(JiraTestListener.class)
public class MyTest extends BaseTest {
    // Tests that fail will automatically create Jira issues
}
```

### Programmatic Jira Integration

```java
JiraClient jiraClient = new JiraClient();

// Create a defect
String issueKey = jiraClient.createDefect(
    "TestName",
    "Error message",
    "Stack trace"
);

// Add a comment
jiraClient.addComment(issueKey, "Additional details");
```

### Publish Test Reports to Confluence

```java
ConfluenceClient client = new ConfluenceClient(baseUrl, email, token, spaceKey);

// Create a test report page
String pageId = client.createTestReportPage(
    "Test Suite Name",
    10,  // passed
    2,   // failed
    1,   // skipped
    "https://allure-report-url"
);
```

## CI/CD

### Jenkins Pipeline

The project includes a `Jenkinsfile` with full CI/CD capabilities:

**Pipeline Stages:**
1. **Checkout** - Clone repository
2. **Build** - Compile project
3. **Run Tests** - Execute selected test type
4. **Generate Allure Report** - Create visual test reports
5. **Create Jira Defects** - Auto-create bugs for failed tests
6. **Publish to Confluence** - Upload test report to Confluence

**Pipeline Parameters:**

| Parameter | Options | Description |
|-----------|---------|-------------|
| `TEST_TYPE` | `all`, `android-local`, `android-browserstack`, `ios-browserstack`, `api`, `atlassian-api` | Select tests to run |
| `CREATE_JIRA_DEFECTS` | `true`/`false` | Auto-create Jira bugs for failures |
| `PUBLISH_TO_CONFLUENCE` | `true`/`false` | Publish test report to Confluence |

**Required Jenkins Credentials:**

| Credential ID | Type | Description |
|--------------|------|-------------|
| `browserstack-username` | Secret text | BrowserStack username |
| `browserstack-accesskey` | Secret text | BrowserStack access key |
| `jira-base-url` | Secret text | Jira Cloud URL |
| `jira-email` | Secret text | Atlassian account email |
| `jira-api-token` | Secret text | Atlassian API token |
| `confluence-base-url` | Secret text | Confluence Cloud URL |

### Running in Jenkins

1. Create a new Pipeline job
2. Point to repository containing `Jenkinsfile`
3. Add credentials in Jenkins (Manage Jenkins → Credentials)
4. Run the pipeline with desired parameters

## Test Reports

### Allure Reports

Generated in `target/allure-results/`. To view:

```bash
mvn allure:serve
```

### Screenshots

Failure screenshots are saved to `target/screenshots/` with timestamp.

## Writing Tests

### Android Test Example

```java
public class MyTest extends BaseTestAndroid {

    @Override
    protected void onDriverInitialized() {
        // Setup after driver is ready
    }

    @Test
    public void testSomething() {
        android().setLocation(new Location(...));
        // test logic
    }
}
```

### iOS Test Example

```java
public class MyIOSTest extends BaseTestiOS {

    @Test
    public void testSomething() {
        ios().executeScript("mobile: ...");
        // test logic
    }
}
```

### Using Data Providers

```java
@Test(dataProvider = "boundaryLocations", dataProviderClass = GeofenceDataProvider.class)
public void testBoundary(GeoLocation location, boolean expectedInside) {
    // data-driven test
}
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Local iOS not supported | iOS testing requires macOS. Use BrowserStack on Windows. |
| BrowserStack auth failed | Verify `BROWSERSTACK_USERNAME` and `BROWSERSTACK_ACCESSKEY` are set |
| Appium connection refused | Ensure Appium server is running on configured port |
| Jira 401 Unauthorized | Check `JIRA_EMAIL` and `JIRA_API_TOKEN` are correct |
| Confluence space not found | Verify `CONFLUENCE_SPACE_KEY` matches an existing space |
| API token invalid | Generate new token at https://id.atlassian.com/manage-profile/security/api-tokens |

## License

MIT

