package com.ftgo.errorhandling.config;

import com.ftgo.errorhandling.handler.GlobalExceptionHandler;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration that activates the FTGO centralized error handling framework.
 *
 * <p>When {@code ftgo-error-handling-lib} is on the classpath of a Spring Boot
 * web application, this configuration automatically registers the
 * {@link GlobalExceptionHandler} as a {@code @RestControllerAdvice} bean.
 *
 * <p>The handler integrates with Micrometer Tracing to populate the
 * {@code traceId} field in every error response. If no {@link Tracer} bean
 * is available (e.g., in test contexts without tracing), a no-op tracer
 * is used and traceId will be {@code null}.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ComponentScan(basePackages = "com.ftgo.errorhandling")
public class FtgoErrorHandlingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    public GlobalExceptionHandler globalExceptionHandler(Tracer tracer) {
        return new GlobalExceptionHandler(tracer);
    }

    /**
     * Provides a no-op Tracer when no tracing infrastructure is configured.
     * This ensures the GlobalExceptionHandler can always be instantiated.
     */
    @Bean
    @ConditionalOnMissingBean(Tracer.class)
    public Tracer noopTracer() {
        return Tracer.NOOP;
    }
}
