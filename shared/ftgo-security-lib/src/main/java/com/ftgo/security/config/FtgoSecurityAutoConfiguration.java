package com.ftgo.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for FTGO security.
 *
 * <p>Importing this configuration (or relying on Spring Boot auto-configuration)
 * sets up the base {@link SecurityFilterChainConfig}, {@link CorsConfig},
 * {@link ActuatorSecurityConfig}, and {@link SecurityExceptionHandler}.
 */
@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties.class)
@Import({
    SecurityFilterChainConfig.class,
    CorsConfig.class,
    ActuatorSecurityConfig.class,
    SecurityExceptionHandler.class,
    SecurityLoggingConfig.class
})
public class FtgoSecurityAutoConfiguration {
}
