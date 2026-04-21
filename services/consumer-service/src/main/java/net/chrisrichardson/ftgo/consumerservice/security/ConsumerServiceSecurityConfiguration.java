package net.chrisrichardson.ftgo.consumerservice.security;

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
 * <p>Extends the shared baseline from {@code libs/ftgo-security} by keeping the
 * consumer REST endpoints authenticated. The shared actuator chain is reused
 * (health/info public, everything else secured).
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
