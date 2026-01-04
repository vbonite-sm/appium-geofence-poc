# Appium Geofence POC

Mobile test automation framework for geofence functionality testing on Android and iOS platforms. Supports local Appium execution and BrowserStack cloud testing with Jenkins CI/CD integration.

## Tech Stack

- **Java 21**
- **Appium Java Client 8.6.0** with Selenium 4.16.1
- **TestNG 7.10.2** - Test framework
- **REST Assured 5.4.0** - API testing
- **Allure 2.25.0** - Test reporting
- **BrowserStack SDK 1.15.0** - Cloud device testing

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
├── test/java/com/geofence/
│   ├── tests/
│   │   ├── BaseTest.java                # Unified base with Template Method
│   │   ├── BaseTestAndroid.java         # Android-specific defaults
│   │   ├── BaseTestiOS.java             # iOS-specific defaults
│   │   ├── GeofenceTest.java            # Android geofence tests
│   │   ├── GeofenceTestiOS.java         # iOS geofence tests
│   │   └── NavigationTest.java
│   ├── api/
│   │   ├── BaseApiTest.java
│   │   └── GeofenceApiTest.java
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

## CI/CD

### Jenkins Pipeline

The project includes a `Jenkinsfile` for automated execution:

```groovy
// Triggered on push to main branch
// Stages: Checkout -> Build -> Test -> Report
```

Pipeline parameters:
- `EXECUTION_MODE`: Target execution environment
- `PLATFORM`: Target platform
- `BROWSERSTACK_USERNAME`: Credential binding
- `BROWSERSTACK_ACCESSKEY`: Credential binding

### Running in Jenkins

1. Create a new Pipeline job
2. Point to repository containing `Jenkinsfile`
3. Configure BrowserStack credentials in Jenkins Credentials store
4. Run the pipeline

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

