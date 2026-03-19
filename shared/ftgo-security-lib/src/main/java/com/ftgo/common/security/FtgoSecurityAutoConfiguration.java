package com.ftgo.common.security;

import com.ftgo.common.security.jwt.JwtSecurityConfiguration;
import com.ftgo.common.security.rbac.MethodSecurityConfiguration;
import com.ftgo.common.security.rbac.ResourceOwnershipEvaluator;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for FTGO shared security.
 * Services that depend on ftgo-security-lib get this configuration
 * automatically via Spring Boot's auto-configuration mechanism.
 *
 * <p>Imports:
 * <ul>
 *   <li>{@link BaseSecurityConfiguration} — filter chains, CORS, session management</li>
 *   <li>{@link SecurityExceptionHandler} — unified 401/403 JSON responses</li>
 *   <li>{@link JwtSecurityConfiguration} — JWT token provider and decoder
 *       (conditional on {@code ftgo.security.jwt.secret} property)</li>
 *   <li>{@link MethodSecurityConfiguration} — method-level security with role hierarchy</li>
 *   <li>{@link ResourceOwnershipEvaluator} — custom permission evaluator for ownership checks</li>
 * </ul>
 */
@AutoConfiguration
@Import({BaseSecurityConfiguration.class, SecurityExceptionHandler.class, JwtSecurityConfiguration.class,
        MethodSecurityConfiguration.class, ResourceOwnershipEvaluator.class})
public class FtgoSecurityAutoConfiguration {
}
