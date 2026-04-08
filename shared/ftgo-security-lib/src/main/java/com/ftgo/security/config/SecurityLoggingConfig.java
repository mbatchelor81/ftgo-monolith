package com.ftgo.security.config;

import com.ftgo.security.filter.SecurityLoggingFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that registers the {@link SecurityLoggingFilter} bean.
 *
 * <p>The filter is enabled by default; disable with
 * {@code ftgo.security.logging.enabled=false}.
 */
@Configuration
@ConditionalOnProperty(name = "ftgo.security.logging.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityLoggingConfig {

    @Bean
    public SecurityLoggingFilter securityLoggingFilter() {
        return new SecurityLoggingFilter();
    }
}
