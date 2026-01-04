package com.geofence.tests.android;

import com.geofence.models.ExecutionMode;
import com.geofence.models.Platform;
import com.geofence.tests.base.BaseTest;
import io.appium.java_client.android.AndroidDriver;

/**
 * Base class for Android-specific tests.
 * Extends BaseTest with Android defaults and convenience methods.
 */
public abstract class BaseTestAndroid extends BaseTest {

    @Override
    protected Platform getDefaultPlatform() {
        return Platform.ANDROID;
    }

    @Override
    protected ExecutionMode getDefaultExecutionMode() {
        return ExecutionMode.LOCAL;
    }

    /**
     * Convenience method to get the Android driver directly.
     */
    protected AndroidDriver android() {
        return getAndroidDriver();
    }

    @Override
    protected void onDriverInitialized() {
        log.debug("Android driver ready: {}", driver.getCapabilities().getPlatformName());
    }
}
