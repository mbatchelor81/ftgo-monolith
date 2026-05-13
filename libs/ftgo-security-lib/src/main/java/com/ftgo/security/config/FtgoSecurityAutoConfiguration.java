package com.ftgo.security.config;

import com.ftgo.security.exception.SecurityExceptionHandlers;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Auto-configuration for Spring Security across FTGO platform services.
 *
 * <p>Provides a sensible default {@link SecurityFilterChain} for stateless REST
 * APIs. Services can override any bean by declaring their own.
 *
 * <p>Disable entirely with {@code ftgo.security.enabled=false}.
 */
@AutoConfiguration(before = SecurityAutoConfiguration.class)
@EnableWebSecurity
@EnableConfigurationProperties(FtgoSecurityProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class FtgoSecurityAutoConfiguration {

    private final FtgoSecurityProperties properties;

    public FtgoSecurityAutoConfiguration(FtgoSecurityProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    @ConditionalOnProperty(name = "ftgo.security.enabled", havingValue = "true", matchIfMissing = true)
    public SecurityFilterChain ftgoSecurityFilterChain(HttpSecurity http,
                                                       SecurityExceptionHandlers securityExceptionHandlers) throws Exception {
        String[] publicPaths = properties.getPublicPaths().toArray(new String[0]);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicPaths).permitAll()
                        .requestMatchers("/actuator/**").authenticated()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(securityExceptionHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(securityExceptionHandlers.accessDeniedHandler()));

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    @ConditionalOnProperty(name = "ftgo.security.enabled", havingValue = "false")
    public SecurityFilterChain ftgoPermitAllFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityExceptionHandlers securityExceptionHandlers() {
        return new SecurityExceptionHandlers();
    }

    @Bean
    @ConditionalOnMissingBean(CorsConfigurationSource.class)
    public CorsConfigurationSource corsConfigurationSource() {
        FtgoSecurityProperties.Cors corsProps = properties.getCors();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(corsProps.getAllowedOrigins());
        configuration.setAllowedMethods(corsProps.getAllowedMethods());
        configuration.setAllowedHeaders(corsProps.getAllowedHeaders());
        configuration.setExposedHeaders(corsProps.getExposedHeaders());
        configuration.setAllowCredentials(corsProps.isAllowCredentials());
        configuration.setMaxAge(corsProps.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
