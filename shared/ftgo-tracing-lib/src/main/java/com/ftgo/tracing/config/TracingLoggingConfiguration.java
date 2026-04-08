package com.ftgo.tracing.config;

import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * Configures structured logging integration with Micrometer Tracing.
 *
 * <p>Micrometer Tracing with Brave automatically populates SLF4J MDC with
 * {@code traceId} and {@code spanId} fields. This configuration class serves
 * as a documentation anchor and can be extended for custom MDC enrichment.
 *
 * <p>The actual MDC population is handled by Brave's {@code MDCScopeDecorator}
 * which is auto-configured by Spring Boot 3.x when the tracing bridge is
 * on the classpath. The logging pattern in {@code application.yml} should
 * reference these fields:
 *
 * <pre>
 * logging:
 *   pattern:
 *     level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
 * </pre>
 */
@Configuration
@ConditionalOnClass(Tracer.class)
public class TracingLoggingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TracingLoggingConfiguration.class);
}
