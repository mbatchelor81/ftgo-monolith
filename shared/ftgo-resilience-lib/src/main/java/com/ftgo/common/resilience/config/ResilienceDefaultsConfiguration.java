package com.ftgo.common.resilience.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Provides default beans used by the resilience infrastructure.
 *
 * <p>Services can override these beans in their own configuration
 * if they need custom timeouts or interceptors.
 */
@Configuration
public class ResilienceDefaultsConfiguration {

    @Bean
    public RestTemplate resilientRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}
