package net.chrisrichardson.ftgo.security;

import net.chrisrichardson.ftgo.security.jwt.FtgoJwtAuthenticationConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Base Spring Security configuration shared across every FTGO microservice.
 *
 * <p>The configuration installs two {@link SecurityFilterChain} beans:
 * <ol>
 *   <li>An actuator chain that keeps {@code /actuator/health} and
 *       {@code /actuator/info} public and requires authentication for every
 *       other actuator endpoint.</li>
 *   <li>An API chain that applies to all remaining routes and requires an
 *       authenticated request. CSRF is disabled (stateless REST APIs), CORS
 *       is enabled, and sessions are set to {@link SessionCreationPolicy#STATELESS}.</li>
 * </ol>
 *
 * <p>Each bean is registered under a stable name so consuming services can
 * replace either chain by declaring its own bean with the same name.
 */
@Configuration
@EnableWebSecurity
public class BaseSecurityConfiguration {

    @Bean("actuatorSecurityFilterChain")
    @Order(1)
    @ConditionalOnMissingBean(name = "actuatorSecurityFilterChain")
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http,
                                                           AuthenticationEntryPoint authenticationEntryPoint,
                                                           AccessDeniedHandler accessDeniedHandler) throws Exception {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            );

        return http.build();
    }

    @Bean("apiSecurityFilterChain")
    @Order(2)
    @ConditionalOnMissingBean(name = "apiSecurityFilterChain")
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
                                                      CorsConfigurationSource corsConfigurationSource,
                                                      AuthenticationEntryPoint authenticationEntryPoint,
                                                      AccessDeniedHandler accessDeniedHandler,
                                                      ObjectProvider<JwtDecoder> jwtDecoderProvider,
                                                      ObjectProvider<FtgoJwtAuthenticationConverter> jwtAuthenticationConverterProvider) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            );

        configureJwt(http, jwtDecoderProvider, jwtAuthenticationConverterProvider,
                authenticationEntryPoint, accessDeniedHandler);

        return http.build();
    }

    /**
     * Enables OAuth2 Resource Server JWT support when a {@link JwtDecoder}
     * is available in the application context (see {@code JwtAuthenticationConfiguration}).
     *
     * <p>Extracted as a static helper so per-service overrides (e.g.
     * {@code OrderServiceSecurityConfiguration}) can reuse the exact same
     * wiring without copy-pasting the configurer chain.
     */
    public static void configureJwt(HttpSecurity http,
                                    ObjectProvider<JwtDecoder> jwtDecoderProvider,
                                    ObjectProvider<FtgoJwtAuthenticationConverter> jwtAuthenticationConverterProvider,
                                    AuthenticationEntryPoint authenticationEntryPoint,
                                    AccessDeniedHandler accessDeniedHandler) throws Exception {
        JwtDecoder decoder = jwtDecoderProvider.getIfAvailable();
        if (decoder == null) {
            return;
        }

        FtgoJwtAuthenticationConverter converter = jwtAuthenticationConverterProvider
                .getIfAvailable(FtgoJwtAuthenticationConverter::new);

        http.oauth2ResourceServer((OAuth2ResourceServerConfigurer<HttpSecurity> oauth2) -> oauth2
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
                .jwt(jwt -> jwt
                        .decoder(decoder)
                        .jwtAuthenticationConverter(converter)
                )
        );
    }

    /**
     * Default CORS configuration. Intentionally permissive for the migration
     * baseline — services are expected to tighten allowed origins, headers
     * and methods via a {@link CorsConfigurationSource} bean of their own.
     */
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
