package com.ftgo.logging.config;

import com.ftgo.logging.aspect.LoggingAspect;
import com.ftgo.logging.aspect.LoggingAspectProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for FTGO structured logging.
 *
 * <p>Registers:
 * <ul>
 *   <li>{@code CorrelationIdFilter} — populates MDC with correlation ID, request ID,
 *       user ID, and HTTP request metadata for every incoming request (servlet environments only).</li>
 *   <li>{@link LoggingAspect} — provides automatic method entry/exit logging for
 *       {@code @Service} classes (enabled via {@code ftgo.logging.aspect.enabled=true}).</li>
 * </ul>
 */
@AutoConfiguration
@EnableConfigurationProperties(LoggingAspectProperties.class)
public class LoggingAutoConfiguration {

    /**
     * Servlet-specific configuration isolated into a nested class so that
     * {@code jakarta.servlet.Filter} is only loaded when the servlet API
     * is actually on the classpath. This prevents {@link NoClassDefFoundError}
     * in non-servlet environments (e.g., reactive gateways, CLI runners).
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(name = "jakarta.servlet.Filter")
    static class ServletLoggingConfiguration {

        @Bean
        public com.ftgo.logging.filter.CorrelationIdFilter correlationIdFilter() {
            return new com.ftgo.logging.filter.CorrelationIdFilter();
        }
    }

    @Bean
    @ConditionalOnProperty(name = "ftgo.logging.aspect.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(name = "org.aspectj.lang.ProceedingJoinPoint")
    public LoggingAspect loggingAspect(LoggingAspectProperties properties) {
        return new LoggingAspect(properties);
    }
}
