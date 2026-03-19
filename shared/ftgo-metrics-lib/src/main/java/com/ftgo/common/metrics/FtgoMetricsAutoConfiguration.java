package com.ftgo.common.metrics;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for FTGO shared metrics.
 * Activates automatically when ftgo-metrics-lib is on the classpath.
 */
@AutoConfiguration
@Import(MetricsConfiguration.class)
public class FtgoMetricsAutoConfiguration {
}
