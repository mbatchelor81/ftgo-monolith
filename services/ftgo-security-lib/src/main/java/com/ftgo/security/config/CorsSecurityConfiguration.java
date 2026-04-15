package com.ftgo.security.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS configuration for FTGO microservices.
 *
 * <p>Configurable via application properties:
 * <ul>
 *   <li>{@code ftgo.security.cors.allowed-origins} - comma-separated list of allowed origins</li>
 *   <li>{@code ftgo.security.cors.allowed-methods} - comma-separated list of allowed HTTP methods</li>
 *   <li>{@code ftgo.security.cors.allowed-headers} - comma-separated list of allowed headers</li>
 *   <li>{@code ftgo.security.cors.allow-credentials} - whether to allow credentials</li>
 *   <li>{@code ftgo.security.cors.max-age} - max age for preflight cache in seconds</li>
 * </ul>
 */
@Configuration
public class CorsSecurityConfiguration {

    @Value("${ftgo.security.cors.allowed-origins:*}")
    private List<String> allowedOrigins;

    @Value("${ftgo.security.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private List<String> allowedMethods;

    @Value("${ftgo.security.cors.allowed-headers:*}")
    private List<String> allowedHeaders;

    @Value("${ftgo.security.cors.allow-credentials:false}")
    private boolean allowCredentials;

    @Value("${ftgo.security.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(allowedHeaders);
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
