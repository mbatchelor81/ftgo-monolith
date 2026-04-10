package com.ftgo.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for FTGO microservices.
 *
 * <p>Configures allowed origins, methods, and headers via externalized
 * properties. Defaults are suitable for development; production deployments
 * should restrict {@code ftgo.security.cors.allowed-origins} to specific
 * domains.
 */
@Configuration
public class CorsConfig {

    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    @Value("${ftgo.security.cors.allowed-origins:*}")
    private String[] allowedOrigins;

    @Value("${ftgo.security.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String[] allowedMethods;

    @Value("${ftgo.security.cors.allowed-headers:Authorization,Content-Type,X-Requested-With,Accept,Origin}")
    private String[] allowedHeaders;

    @Value("${ftgo.security.cors.exposed-headers:}")
    private String[] exposedHeaders;

    @Value("${ftgo.security.cors.allow-credentials:false}")
    private boolean allowCredentials;

    @Value("${ftgo.security.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CORS: origins={}, methods={}",
            Arrays.toString(allowedOrigins), Arrays.toString(allowedMethods));

        CorsConfiguration configuration = new CorsConfiguration();
        if (Arrays.asList(allowedOrigins).contains("*")) {
            configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
        } else {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        }
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        if (exposedHeaders.length > 0 && !exposedHeaders[0].isEmpty()) {
            configuration.setExposedHeaders(Arrays.asList(exposedHeaders));
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
