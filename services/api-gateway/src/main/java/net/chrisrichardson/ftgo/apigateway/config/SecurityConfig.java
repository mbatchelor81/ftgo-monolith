package net.chrisrichardson.ftgo.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Reactive security configuration that enables JWT validation for every
 * request flowing through the gateway.
 *
 * <p>The gateway acts as an OAuth2 Resource Server: incoming requests must
 * present a bearer token whose issuer and signing key match
 * {@code spring.security.oauth2.resourceserver.jwt.*} configuration.
 *
 * <p>A small allowlist (actuator health probes, login, and public docs) is
 * left open so platform-level liveness checks and the initial auth exchange
 * keep working without a token.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/prometheus",
                                "/api/auth/**",
                                "/api/public/**"
                        ).permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> { }))
                .build();
    }
}
