package com.ftgo.security.config;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Base Spring Security configuration for FTGO microservices.
 *
 * <p>Provides a default {@link SecurityFilterChain} bean that:
 * <ul>
 *   <li>Disables CSRF (stateless REST APIs with no browser session)</li>
 *   <li>Configures stateless session management</li>
 *   <li>Permits public access to health endpoints and OpenAPI docs</li>
 *   <li>Requires authentication for all other endpoints</li>
 *   <li>Enables CORS using the configured {@code CorsConfigurationSource}</li>
 *   <li>Wires custom JSON error responses for 401/403</li>
 *   <li>Enables HTTP Basic authentication as a baseline</li>
 * </ul>
 *
 * <p>This chain runs at {@code @Order(2)}, after the actuator filter chain
 * ({@code @Order(1)}). Services can override security by declaring their
 * own {@code SecurityFilterChain} bean at a different order.
 *
 * <p>Uses Spring Security 6.x lambda DSL (not deprecated
 * {@code WebSecurityConfigurerAdapter}).
 */
@Configuration
@EnableWebSecurity
public class SecurityFilterChainConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityFilterChainConfig.class);

    @Value("${ftgo.security.public-paths:/actuator/health,/actuator/info,/v3/api-docs/**,/swagger-ui/**,/swagger-ui.html}")
    private String[] publicPaths;

    private final List<ServiceSecurityConfigurer> serviceConfigurers;

    public SecurityFilterChainConfig(
            @Autowired(required = false) List<ServiceSecurityConfigurer> serviceConfigurers) {
        this.serviceConfigurers = serviceConfigurers != null ? serviceConfigurers : Collections.emptyList();
    }

    /**
     * Default security filter chain for FTGO microservices.
     *
     * @param http the HttpSecurity builder
     * @param authenticationEntryPoint custom JSON 401 handler
     * @param accessDeniedHandler custom JSON 403 handler
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http,
            AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler) throws Exception {
        log.info("Configuring FTGO default security filter chain");

        http
            // Disable CSRF — stateless REST APIs do not use cookies/sessions
            .csrf(AbstractHttpConfigurer::disable)

            // Enable CORS using the CorsConfigurationSource bean
            .cors(Customizer.withDefaults())

            // Stateless session management — no HTTP session created
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Custom JSON error responses
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )

            // Authorization rules
            .authorizeHttpRequests(auth -> {
                // Public endpoints
                auth.requestMatchers(publicPaths).permitAll();
                // Allow CORS preflight requests
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                // Apply per-service authorization rules BEFORE anyRequest()
                for (ServiceSecurityConfigurer configurer : serviceConfigurers) {
                    log.info("Applying authorization rules from service: {}", configurer.serviceName());
                    configurer.configureAuthorization(auth);
                }
                // All other requests require authentication (must be last)
                auth.anyRequest().authenticated();
            })

            // HTTP Basic auth as baseline (JWT can be layered on top)
            // Pass custom entry point so failed credentials return JSON, not HTML
            .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(authenticationEntryPoint));

        // Apply per-service HttpSecurity customizations (filters, OAuth2, etc.)
        for (ServiceSecurityConfigurer configurer : serviceConfigurers) {
            log.info("Applying HTTP security customization from service: {}", configurer.serviceName());
            configurer.configureHttpSecurity(http);
        }

        return http.build();
    }
}
