package com.ftgo.security.config;

import com.ftgo.security.jwt.JwtAuthenticationConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Base security configuration for FTGO microservices.
 *
 * <p>Provides a default {@link SecurityFilterChain} that:
 * <ul>
 *   <li>Requires authentication on all endpoints by default</li>
 *   <li>Disables CSRF for stateless REST APIs</li>
 *   <li>Uses stateless session management</li>
 *   <li>Configures OAuth2 Resource Server with JWT validation when a
 *       {@link JwtAuthenticationConverter} is available</li>
 *   <li>Falls back to HTTP Basic when JWT is not configured</li>
 * </ul>
 *
 * <p>Individual services can override this bean to customize their security rules.
 */
@Configuration
@EnableWebSecurity
public class BaseSecurityConfiguration {

    @Autowired(required = false)
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    @Order(100)
    @ConditionalOnMissingBean(name = "serviceSecurityFilterChain")
    public SecurityFilterChain baseSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated());

        if (jwtAuthenticationConverter != null) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        } else {
            http.httpBasic(Customizer.withDefaults());
        }

        return http.build();
    }
}
