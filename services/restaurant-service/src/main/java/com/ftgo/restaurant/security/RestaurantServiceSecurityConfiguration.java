package com.ftgo.restaurant.security;

import net.chrisrichardson.ftgo.security.BaseSecurityConfiguration;
import net.chrisrichardson.ftgo.security.SecurityExceptionHandler;
import net.chrisrichardson.ftgo.security.jwt.FtgoJwtAuthenticationConverter;
import org.springframework.beans.factory.ObjectProvider;
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
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Restaurant service security overrides.
 *
 * <p>Lives in {@code com.ftgo.restaurant.security} so it is picked up by the
 * {@code @SpringBootApplication} component scan on
 * {@code com.ftgo.restaurant.RestaurantServiceApplication}. Keeps
 * {@code /restaurants/**} and {@code /api/**} authenticated, layers
 * JWT-based OAuth2 Resource Server support on top (see EM-40), and reuses
 * the shared actuator chain from {@code libs/ftgo-security}.
 */
@Configuration
@EnableWebSecurity
public class RestaurantServiceSecurityConfiguration {

    @Bean("apiSecurityFilterChain")
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
                                                      CorsConfigurationSource corsConfigurationSource,
                                                      SecurityExceptionHandler securityExceptionHandler,
                                                      ObjectProvider<JwtDecoder> jwtDecoderProvider,
                                                      ObjectProvider<FtgoJwtAuthenticationConverter> jwtAuthenticationConverterProvider) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/restaurants/**", "/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(securityExceptionHandler)
                .accessDeniedHandler(securityExceptionHandler)
            );

        BaseSecurityConfiguration.configureJwt(http,
                jwtDecoderProvider, jwtAuthenticationConverterProvider,
                securityExceptionHandler, securityExceptionHandler);

        return http.build();
    }
}
