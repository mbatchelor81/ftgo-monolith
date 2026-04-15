package com.ftgo.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Spring Boot Actuator endpoints.
 *
 * <p>This configuration is activated only when the actuator is on the classpath. It applies a
 * higher-priority filter chain scoped to {@code /actuator/**} that:
 *
 * <ul>
 *   <li>Permits unauthenticated access to {@code /actuator/health} and {@code /actuator/info}
 *   <li>Requires authentication for all other actuator endpoints
 * </ul>
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.web.WebEndpointResponse")
public class ActuatorSecurityConfiguration {

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/actuator/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/actuator/health",
                                                "/actuator/health/**",
                                                "/actuator/info")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
