package com.ftgo.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS configuration for FTGO microservices.
 *
 * <p>Configures allowed origins, methods, and headers via externalized
 * properties bound through {@link SecurityProperties}. Defaults are suitable
 * for development; production deployments should restrict
 * {@code ftgo.security.cors.allowed-origins} to specific domains.
 */
@Configuration
public class CorsConfig {

    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    private final SecurityProperties securityProperties;

    public CorsConfig(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        SecurityProperties.Cors cors = securityProperties.getCors();

        log.info("Configuring CORS: origins={}, methods={}",
            cors.getAllowedOrigins(), cors.getAllowedMethods());

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(cors.getAllowedOrigins());
        configuration.setAllowedMethods(cors.getAllowedMethods());
        configuration.setAllowedHeaders(cors.getAllowedHeaders());
        configuration.setAllowCredentials(cors.isAllowCredentials());
        configuration.setMaxAge(cors.getMaxAge());

        if (!cors.getExposedHeaders().isEmpty()) {
            configuration.setExposedHeaders(cors.getExposedHeaders());
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
