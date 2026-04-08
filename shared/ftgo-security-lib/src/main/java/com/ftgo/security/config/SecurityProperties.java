package com.ftgo.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Externalized security configuration properties for FTGO microservices.
 *
 * <p>Bind these in {@code application.yml} under the {@code ftgo.security} prefix:
 *
 * <pre>
 * ftgo:
 *   security:
 *     public-paths:
 *       - /actuator/health
 *       - /actuator/info
 *       - /v3/api-docs/**
 *     cors:
 *       allowed-origins:
 *         - https://example.com
 *       allowed-methods:
 *         - GET
 *         - POST
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.security")
public class SecurityProperties {

    /**
     * URL patterns that are publicly accessible without authentication.
     */
    private List<String> publicPaths = new ArrayList<>(List.of(
        "/actuator/health",
        "/actuator/info",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    ));

    /**
     * CORS configuration properties.
     */
    private Cors cors = new Cors();

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    /**
     * CORS-specific properties nested under {@code ftgo.security.cors}.
     */
    public static class Cors {

        private List<String> allowedOrigins = new ArrayList<>(List.of("*"));
        private List<String> allowedMethods = new ArrayList<>(List.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        private List<String> allowedHeaders = new ArrayList<>(List.of(
            "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"
        ));
        private List<String> exposedHeaders = new ArrayList<>();
        private boolean allowCredentials = false;
        private long maxAge = 3600;

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public List<String> getExposedHeaders() {
            return exposedHeaders;
        }

        public void setExposedHeaders(List<String> exposedHeaders) {
            this.exposedHeaders = exposedHeaders;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }
    }
}
