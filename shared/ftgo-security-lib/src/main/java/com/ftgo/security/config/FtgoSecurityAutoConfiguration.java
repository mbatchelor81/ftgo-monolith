package com.ftgo.security.config;

import com.ftgo.security.authorization.RoleHierarchyConfig;
import com.ftgo.security.jwt.JwtSecurityConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for FTGO security.
 *
 * <p>Importing this configuration (or relying on Spring Boot auto-configuration)
 * sets up the base {@link SecurityFilterChainConfig}, {@link CorsConfig},
 * {@link ActuatorSecurityConfig}, {@link SecurityExceptionHandler},
 * optional JWT authentication via {@link JwtSecurityConfiguration},
 * and role-based authorization via {@link RoleHierarchyConfig}.
 */
@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties.class)
@Import({
    SecurityFilterChainConfig.class,
    CorsConfig.class,
    ActuatorSecurityConfig.class,
    SecurityExceptionHandler.class,
    SecurityLoggingConfig.class,
    JwtSecurityConfiguration.class,
    RoleHierarchyConfig.class
})
public class FtgoSecurityAutoConfiguration {
}
