package com.ftgo.security.config;

import com.ftgo.security.jwt.JwtConfiguration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for the FTGO security library.
 *
 * <p>Services that depend on {@code ftgo-security-lib} can import this configuration
 * to activate the full security stack: base filter chain, actuator security,
 * CORS, JWT authentication, and REST exception handlers.
 */
@Configuration
@Import({
    BaseSecurityConfiguration.class,
    ActuatorSecurityConfiguration.class,
    CorsSecurityConfiguration.class,
    JwtConfiguration.class
})
@ComponentScan(basePackages = "com.ftgo.security.handler")
public class FtgoSecurityAutoConfiguration {
}
