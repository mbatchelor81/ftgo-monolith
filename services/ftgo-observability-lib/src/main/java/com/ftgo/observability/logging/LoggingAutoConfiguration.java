package com.ftgo.observability.logging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for structured logging across all FTGO services.
 *
 * <p>Registers servlet filters that populate SLF4J MDC with request context (correlation ID,
 * service name, request method/URI). These MDC values are automatically included in structured JSON
 * log output by the logstash-logback-encoder configured in {@code logback-spring.xml}.
 *
 * <p>This configuration is only activated in web application contexts where the servlet API is
 * available.
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "jakarta.servlet.Filter")
public class LoggingAutoConfiguration {

    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }

    @Bean
    public ServiceMdcFilter serviceMdcFilter(
            @org.springframework.beans.factory.annotation.Value(
                            "${spring.application.name:unknown}")
                    String serviceName) {
        return new ServiceMdcFilter(serviceName);
    }
}
