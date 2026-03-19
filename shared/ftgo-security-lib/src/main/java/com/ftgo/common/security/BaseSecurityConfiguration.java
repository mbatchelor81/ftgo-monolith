package com.ftgo.common.security;

import com.ftgo.common.security.jwt.JwtAuthenticationConverter;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Base Spring Security configuration shared across all FTGO microservices.
 *
 * <p>Provides a default {@link SecurityFilterChain} that:
 * <ul>
 *   <li>Disables CSRF (stateless REST APIs)</li>
 *   <li>Enables CORS with configurable origins</li>
 *   <li>Uses stateless session management</li>
 *   <li>Permits unauthenticated access to actuator health and info endpoints</li>
 *   <li>Requires authentication for all other endpoints</li>
 *   <li>Supports JWT bearer token authentication when a {@link JwtDecoder} bean is available</li>
 *   <li>Falls back to HTTP Basic authentication when JWT is not configured</li>
 * </ul>
 *
 * <p>Services can override this bean by defining their own {@link SecurityFilterChain}.
 */
@Configuration
@EnableWebSecurity
public class BaseSecurityConfiguration {

    private final SecurityExceptionHandler securityExceptionHandler;
    private final ObjectProvider<JwtDecoder> jwtDecoderProvider;
    private final ObjectProvider<JwtAuthenticationConverter> jwtConverterProvider;

    public BaseSecurityConfiguration(
            SecurityExceptionHandler securityExceptionHandler,
            ObjectProvider<JwtDecoder> jwtDecoderProvider,
            ObjectProvider<JwtAuthenticationConverter> jwtConverterProvider) {
        this.securityExceptionHandler = securityExceptionHandler;
        this.jwtDecoderProvider = jwtDecoderProvider;
        this.jwtConverterProvider = jwtConverterProvider;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(securityExceptionHandler)
                .accessDeniedHandler(securityExceptionHandler)
            )
            .httpBasic(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    @Order(2)
    @ConditionalOnMissingBean(name = "apiSecurityFilterChain")
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(securityExceptionHandler)
                .accessDeniedHandler(securityExceptionHandler)
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Use JWT bearer token auth when a JwtDecoder is available; fall back to HTTP Basic
        JwtDecoder decoder = jwtDecoderProvider.getIfAvailable();
        if (decoder != null) {
            JwtAuthenticationConverter converter = jwtConverterProvider.getIfAvailable();
            http.oauth2ResourceServer(oauth2 -> {
                oauth2.jwt(jwt -> {
                    jwt.decoder(decoder);
                    if (converter != null) {
                        jwt.jwtAuthenticationConverter(converter);
                    }
                });
            });
        } else {
            http.httpBasic(Customizer.withDefaults());
        }

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
