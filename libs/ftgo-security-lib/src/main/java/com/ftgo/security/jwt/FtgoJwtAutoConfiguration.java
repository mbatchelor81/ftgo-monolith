package com.ftgo.security.jwt;

import com.ftgo.security.config.FtgoSecurityAutoConfiguration;
import com.ftgo.security.config.FtgoSecurityProperties;
import com.ftgo.security.exception.SecurityExceptionHandlers;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Auto-configuration for JWT-based OAuth2 Resource Server authentication.
 *
 * <p>Activated when {@code ftgo.security.jwt.enabled=true}. Configures the
 * service as an OAuth2 Resource Server that validates JWT tokens issued by
 * the configured identity provider (e.g. Keycloak).
 *
 * <p>When JWT mode is active, this configuration takes precedence over
 * the base HTTP Basic configuration in
 * {@link com.ftgo.security.config.FtgoSecurityAutoConfiguration}.
 */
@AutoConfiguration(before = FtgoSecurityAutoConfiguration.class)
@EnableWebSecurity
@EnableConfigurationProperties({FtgoJwtProperties.class, FtgoSecurityProperties.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "ftgo.security.jwt.enabled", havingValue = "true")
public class FtgoJwtAutoConfiguration {

    private final FtgoJwtProperties jwtProperties;
    private final FtgoSecurityProperties securityProperties;

    public FtgoJwtAutoConfiguration(FtgoJwtProperties jwtProperties,
                                     FtgoSecurityProperties securityProperties) {
        this.jwtProperties = jwtProperties;
        this.securityProperties = securityProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtClaimsExtractor jwtClaimsExtractor() {
        return new JwtClaimsExtractor(jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public FtgoJwtAuthenticationConverter ftgoJwtAuthenticationConverter(JwtClaimsExtractor claimsExtractor) {
        return new FtgoJwtAuthenticationConverter(claimsExtractor, jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenRefreshService tokenRefreshService() {
        return new TokenRefreshService(jwtProperties.getTokenRefresh());
    }

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = jwtProperties.getJwkSetUri();
        String issuerUri = jwtProperties.getIssuerUri();

        NimbusJwtDecoder decoder;
        if (jwkSetUri != null && !jwkSetUri.isBlank()) {
            decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        } else if (issuerUri != null && !issuerUri.isBlank()) {
            decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);
        } else {
            throw new IllegalStateException(
                    "Either ftgo.security.jwt.issuer-uri or ftgo.security.jwt.jwk-set-uri must be configured");
        }

        if (issuerUri != null && !issuerUri.isBlank()) {
            decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
        }

        return decoder;
    }

    @Bean
    public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http,
                                                      FtgoJwtAuthenticationConverter jwtConverter,
                                                      SecurityExceptionHandlers securityExceptionHandlers) throws Exception {
        String[] publicPaths = securityProperties.getPublicPaths().toArray(new String[0]);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicPaths).permitAll()
                        .requestMatchers("/actuator/**").authenticated()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter))
                        .authenticationEntryPoint(securityExceptionHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(securityExceptionHandlers.accessDeniedHandler()))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(securityExceptionHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(securityExceptionHandlers.accessDeniedHandler()));

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityExceptionHandlers securityExceptionHandlers() {
        return new SecurityExceptionHandlers();
    }
}
