package com.ftgo.security.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Interface for per-service security customization.
 *
 * <p>Microservices can implement this interface and register it as a Spring
 * bean to add service-specific authorization rules and HTTP security
 * customizations on top of the base security configuration.
 *
 * <p>Two extension points are provided:
 * <ul>
 *   <li>{@link #configureAuthorization} — for adding request matcher rules
 *       (called <em>before</em> the {@code anyRequest().authenticated()} catch-all)</li>
 *   <li>{@link #configureHttpSecurity} — for other {@link HttpSecurity} customizations
 *       such as adding filters, configuring OAuth2, etc. (called after the base
 *       configuration but before {@code http.build()})</li>
 * </ul>
 *
 * <p>Example usage in a service:
 * <pre>{@code
 * @Component
 * public class OrderServiceSecurityConfigurer implements ServiceSecurityConfigurer {
 *     @Override
 *     public void configureAuthorization(
 *             AuthorizeHttpRequestsConfigurer<HttpSecurity>
 *                 .AuthorizationManagerRequestMatcherRegistry auth) {
 *         auth.requestMatchers("/api/orders/public/**").permitAll();
 *     }
 *
 *     @Override
 *     public String serviceName() {
 *         return "order-service";
 *     }
 * }
 * }</pre>
 */
public interface ServiceSecurityConfigurer {

    /**
     * Add service-specific authorization rules to the request matcher registry.
     *
     * <p>This method is called <em>inside</em> the {@code authorizeHttpRequests}
     * block, <em>before</em> the {@code anyRequest().authenticated()} catch-all.
     * Implementations should only call {@code requestMatchers(...).permitAll()}
     * or similar — do not call {@code anyRequest()} here.
     *
     * @param auth the authorization registry to add rules to
     */
    default void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>
                .AuthorizationManagerRequestMatcherRegistry auth) {
        // no-op by default
    }

    /**
     * Apply additional {@link HttpSecurity} customizations beyond authorization rules.
     *
     * <p>Called after the base security configuration (CSRF, CORS, session management,
     * exception handling, authorization, HTTP Basic) but before {@code http.build()}.
     * Use this for adding custom filters, configuring OAuth2 resource server, etc.
     *
     * @param http the HttpSecurity builder to customize
     * @throws Exception if an error occurs during configuration
     */
    default void configureHttpSecurity(HttpSecurity http) throws Exception {
        // no-op by default
    }

    /**
     * Returns the name of the service for logging and diagnostics.
     */
    String serviceName();
}
