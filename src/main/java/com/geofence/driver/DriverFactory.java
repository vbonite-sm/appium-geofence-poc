package com.geofence.driver;

import com.geofence.config.EnvironmentConfig;
import com.geofence.models.ExecutionMode;
import com.geofence.models.Platform;
import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating Appium drivers based on platform and execution mode.
 */
public class DriverFactory {

    private static final Logger log = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    public static AppiumDriver createDriver(Platform platform, ExecutionMode mode, EnvironmentConfig config) throws Exception {
        log.info("Creating {} driver in {} mode", platform, mode);

        DriverProvider provider = getProvider(platform, mode, config);
        AppiumDriver driver = provider.createDriver();

        log.info("Driver created successfully for {} on {}", platform, mode);
        return driver;
    }

    public static AppiumDriver createDriver(Platform platform, ExecutionMode mode) throws Exception {
        return createDriver(platform, mode, EnvironmentConfig.getInstance());
    }

    private static DriverProvider getProvider(Platform platform, ExecutionMode mode, EnvironmentConfig config) {
        return switch (mode) {
            case LOCAL -> new LocalDriverProvider(config, platform);
            case BROWSERSTACK -> new BrowserStackDriverProvider(config, platform);
            case SAUCELABS -> throw new UnsupportedOperationException("SauceLabs not yet implemented");
        };
    }
}
