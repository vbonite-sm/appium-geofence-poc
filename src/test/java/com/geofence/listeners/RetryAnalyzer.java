package com.geofence.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retry analyzer for flaky tests.
 * Configurable via system property 'test.retry.max' (default: 2).
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(RetryAnalyzer.class);
    private static final int DEFAULT_MAX_RETRY = 2;

    private int retryCount = 0;
    private final int maxRetryCount;

    public RetryAnalyzer() {
        String maxRetry = System.getProperty("test.retry.max");
        this.maxRetryCount = maxRetry != null ? Integer.parseInt(maxRetry) : DEFAULT_MAX_RETRY;
    }

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetryCount) {
            retryCount++;
            log.info("Retrying test '{}' - attempt {}/{}", 
                    result.getName(), retryCount, maxRetryCount);
            return true;
        }
        return false;
    }

    public int getRetryCount() {
        return retryCount;
    }
}
