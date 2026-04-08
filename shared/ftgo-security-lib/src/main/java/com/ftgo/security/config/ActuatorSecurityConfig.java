package com.ftgo.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Security configuration for Spring Boot Actuator endpoints.
 *
 * <p>This filter chain has a higher precedence ({@code @Order(1)}) than the
 * default filter chain so that actuator endpoints are matched first.
 *
 * <ul>
 *   <li>{@code /actuator/health} and {@code /actuator/info} — public (no auth required)</li>
 *   <li>All other actuator endpoints — require authentication</li>
 * </ul>
 */
@Configuration
public class ActuatorSecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(ActuatorSecurityConfig.class);

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(
            HttpSecurity http,
            AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler) throws Exception {
        log.info("Configuring actuator endpoint security");

        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                // Health and info endpoints are public
                .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class)).permitAll()
                // All other actuator endpoints require authentication
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(authenticationEntryPoint));

        return http.build();
    }
}
