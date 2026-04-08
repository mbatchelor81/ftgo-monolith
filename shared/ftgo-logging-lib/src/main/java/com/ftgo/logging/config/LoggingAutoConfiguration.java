package com.ftgo.logging.config;

import com.ftgo.logging.filter.CorrelationIdFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for FTGO structured logging.
 *
 * <p>Registers the {@link CorrelationIdFilter} as a servlet filter bean so that
 * every incoming HTTP request is tagged with a correlation ID in the MDC. This
 * enables cross-service log correlation when the API Gateway sets the
 * {@code X-Correlation-ID} header.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class LoggingAutoConfiguration {

    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }
}
