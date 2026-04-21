package net.chrisrichardson.ftgo.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Global CORS configuration applied ahead of the gateway's route filters.
 *
 * <p>Allowed origins default to the local Vite/React dev server and are
 * overridable per environment via {@code ftgo.gateway.cors.allowed-origins}.
 * We deliberately avoid wildcard origins so credentialed browser requests
 * cannot be made from arbitrary sites.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${ftgo.gateway.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
            List<String> allowedOrigins,
            @Value("${ftgo.gateway.cors.max-age-seconds:3600}") long maxAgeSeconds) {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Correlation-Id"));
        config.setExposedHeaders(List.of("X-Correlation-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(maxAgeSeconds);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
