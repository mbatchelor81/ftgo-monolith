package com.ftgo.errorhandling.config;

import com.ftgo.errorhandling.handler.GlobalExceptionHandler;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that registers the {@link GlobalExceptionHandler} for any Spring Boot service
 * that includes {@code ftgo-error-handling-lib} on its classpath.
 */
@AutoConfiguration
public class ErrorHandlingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(Tracer tracer) {
        return new GlobalExceptionHandler(tracer);
    }
}
