package com.ftgo.common.security.jwt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * Auto-configuration for JWT-based authentication.
 *
 * <p>Activated when {@code ftgo.security.jwt.secret} is set in application properties.
 * Provides:
 * <ul>
 *   <li>{@link JwtProperties} — configurable JWT settings</li>
 *   <li>{@link JwtTokenProvider} — token generation and validation</li>
 *   <li>{@link JwtDecoder} — Spring Security OAuth2 Resource Server integration</li>
 *   <li>{@link JwtAuthenticationConverter} — maps JWT claims to Spring authorities</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@ConditionalOnProperty(prefix = "ftgo.security.jwt", name = "secret")
public class JwtSecurityConfiguration {

    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtProperties properties) {
        return new JwtTokenProvider(properties);
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtTokenProvider tokenProvider) {
        return tokenProvider.getJwtDecoder();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        return new JwtAuthenticationConverter();
    }
}
