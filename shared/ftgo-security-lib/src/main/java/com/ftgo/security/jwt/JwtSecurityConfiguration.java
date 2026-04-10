package com.ftgo.security.jwt;

import com.ftgo.security.exception.SecurityConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that creates JWT-related beans when JWT authentication
 * is enabled via {@code ftgo.security.jwt.enabled=true}.
 *
 * <p>Validates that the signing secret meets minimum security requirements
 * (at least 32 bytes for HMAC-SHA256) and creates the {@link JwtTokenProvider}
 * and {@link JwtAuthenticationFilter} beans.
 */
@Configuration
@ConditionalOnProperty(name = "ftgo.security.jwt.enabled", havingValue = "true")
@EnableConfigurationProperties(JwtProperties.class)
public class JwtSecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JwtSecurityConfiguration.class);
    private static final int MINIMUM_SECRET_LENGTH = 32;

    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtProperties properties) {
        validateSecret(properties);
        log.info("JWT authentication enabled: issuer={}, accessTokenExpiration={}s, refreshTokenExpiration={}s",
            properties.getIssuer(), properties.getExpiration(), properties.getRefreshExpiration());
        return new JwtTokenProvider(properties);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    private void validateSecret(JwtProperties properties) {
        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            throw new SecurityConfigurationException(
                "JWT signing secret must be configured via ftgo.security.jwt.secret "
                + "(use FTGO_JWT_SECRET environment variable in production)");
        }
        if (properties.getSecret().length() < MINIMUM_SECRET_LENGTH) {
            throw new SecurityConfigurationException(
                "JWT signing secret must be at least " + MINIMUM_SECRET_LENGTH
                + " characters (256 bits) for HMAC-SHA256 signing");
        }
    }
}
