package com.ftgo.tracing.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration that activates all FTGO distributed tracing components.
 *
 * <p>Importing this configuration (or depending on ftgo-tracing-lib with
 * Spring Boot auto-configuration) provides:
 * <ul>
 *   <li>Micrometer Tracing with Brave bridge</li>
 *   <li>Zipkin reporter for trace export</li>
 *   <li>Structured logging with traceId/spanId in MDC</li>
 *   <li>{@code @Traced} annotation support for custom business spans</li>
 * </ul>
 *
 * <p>Configuration is driven by {@code application.yml} properties under
 * {@code management.tracing.*} and {@code management.zipkin.tracing.*}.
 */
@Configuration
@ComponentScan(basePackages = "com.ftgo.tracing")
@Import({
    TracingConfiguration.class,
    TracingLoggingConfiguration.class
})
public class FtgoTracingAutoConfiguration {
}
