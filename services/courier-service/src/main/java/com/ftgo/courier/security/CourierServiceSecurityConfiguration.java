package com.ftgo.courier.security;

import net.chrisrichardson.ftgo.security.SecurityExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Courier service security overrides.
 *
 * <p>Lives in {@code com.ftgo.courier.security} so it is picked up by the
 * {@code @SpringBootApplication} component scan on
 * {@code com.ftgo.courier.CourierServiceApplication}. The shared actuator
 * chain from {@code libs/ftgo-security} is reused as-is.
 */
@Configuration
@EnableWebSecurity
public class CourierServiceSecurityConfiguration {

    @Bean("apiSecurityFilterChain")
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
                                                      CorsConfigurationSource corsConfigurationSource,
                                                      SecurityExceptionHandler securityExceptionHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/couriers/**", "/api/**").authenticated()
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

        return http.build();
    }
}
