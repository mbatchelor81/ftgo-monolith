package com.ftgo.consumer.security;

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
 * Consumer service security overrides.
 *
 * <p>Lives in {@code com.ftgo.consumer.security} so it is picked up by the
 * {@code @SpringBootApplication} component scan on
 * {@code com.ftgo.consumer.ConsumerServiceApplication}. The shared actuator
 * chain from {@code libs/ftgo-security} is reused as-is.
 */
@Configuration
@EnableWebSecurity
public class ConsumerServiceSecurityConfiguration {

    @Bean("apiSecurityFilterChain")
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
                                                      CorsConfigurationSource corsConfigurationSource,
                                                      SecurityExceptionHandler securityExceptionHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/consumers/**", "/api/**").authenticated()
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
