package com.ftgo.common.tracing;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for FTGO distributed tracing.
 * Services that depend on ftgo-tracing-lib get tracing configured
 * automatically via Spring Boot's auto-configuration mechanism.
 */
@AutoConfiguration
@Import(TracingConfiguration.class)
public class FtgoTracingAutoConfiguration {
}
