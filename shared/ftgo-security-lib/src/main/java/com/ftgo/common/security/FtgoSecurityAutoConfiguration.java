package com.ftgo.common.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for FTGO shared security.
 * Services that depend on ftgo-security-lib get this configuration
 * automatically via Spring Boot's auto-configuration mechanism.
 */
@AutoConfiguration
@Import({BaseSecurityConfiguration.class, SecurityExceptionHandler.class})
public class FtgoSecurityAutoConfiguration {
}
