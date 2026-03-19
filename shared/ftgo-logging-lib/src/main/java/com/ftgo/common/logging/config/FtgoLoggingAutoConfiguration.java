package com.ftgo.common.logging.config;

import com.ftgo.common.logging.filter.CorrelationIdMdcFilter;
import com.ftgo.common.logging.filter.RequestLoggingMdcFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for FTGO structured logging.
 * Registers MDC filters for correlation ID propagation and request context logging.
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.ftgo.common.logging")
@ConditionalOnWebApplication
public class FtgoLoggingAutoConfiguration {

    @Bean
    public CorrelationIdMdcFilter correlationIdMdcFilter() {
        return new CorrelationIdMdcFilter();
    }

    @Bean
    public RequestLoggingMdcFilter requestLoggingMdcFilter() {
        return new RequestLoggingMdcFilter();
    }
}
