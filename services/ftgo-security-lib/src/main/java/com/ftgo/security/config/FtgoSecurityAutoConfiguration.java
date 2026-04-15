package com.ftgo.security.config;

import com.ftgo.security.authorization.MethodSecurityConfiguration;
import com.ftgo.security.authorization.RoleHierarchyConfiguration;
import com.ftgo.security.jwt.JwtConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for the FTGO security library.
 *
 * <p>Services that depend on {@code ftgo-security-lib} can import this configuration to activate
 * the full security stack: base filter chain, actuator security, CORS, JWT authentication, REST
 * exception handlers, role hierarchy, and method-level authorization.
 */
@Configuration
@Import({
    BaseSecurityConfiguration.class,
    ActuatorSecurityConfiguration.class,
    CorsSecurityConfiguration.class,
    JwtConfiguration.class,
    RoleHierarchyConfiguration.class,
    MethodSecurityConfiguration.class
})
@ComponentScan(basePackages = "com.ftgo.security.handler")
public class FtgoSecurityAutoConfiguration {}
