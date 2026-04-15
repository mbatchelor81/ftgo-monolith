package com.ftgo.apigateway.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * CORS configuration for the reactive API Gateway.
 *
 * <p>Configurable via application properties under {@code ftgo.security.cors.*}.
 */
@Configuration
public class GatewayCorsConfiguration {

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
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(allowedOrigins);
        corsConfig.setAllowedMethods(allowedMethods);
        corsConfig.setAllowedHeaders(allowedHeaders);
        corsConfig.setAllowCredentials(allowCredentials);
        corsConfig.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsWebFilter(source);
    }
}
