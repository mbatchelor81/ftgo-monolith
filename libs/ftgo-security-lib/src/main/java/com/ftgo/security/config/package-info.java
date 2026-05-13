/**
 * Spring Security auto-configuration for the FTGO platform.
 *
 * <p>Provides a default {@link org.springframework.security.web.SecurityFilterChain}
 * with sensible defaults for stateless REST APIs: CSRF disabled, CORS configured,
 * actuator health endpoints public, and all other endpoints requiring authentication.
 *
 * <p>Services can override any bean by declaring their own.
 */
package com.ftgo.security.config;
