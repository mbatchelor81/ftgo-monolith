package com.ftgo.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Gateway filter that extracts claims from a validated JWT and propagates them as HTTP headers to
 * downstream services.
 *
 * <p>Propagated headers:
 *
 * <ul>
 *   <li>{@code X-User-Id} — the {@code userId} claim
 *   <li>{@code X-User-Name} — the {@code sub} (subject) claim
 *   <li>{@code X-User-Roles} — comma-separated list of roles
 *   <li>{@code X-User-Permissions} — comma-separated list of permissions
 * </ul>
 *
 * <p>This allows downstream services to access identity information without re-validating the JWT.
 */
@Component
public class JwtClaimsPropagationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtClaimsPropagationGatewayFilterFactory.Config> {

    private static final Logger LOG =
            LoggerFactory.getLogger(JwtClaimsPropagationGatewayFilterFactory.class);

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_NAME = "X-User-Name";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";

    public JwtClaimsPropagationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedJwtClaimsPropagationFilter();
    }

    private static final class OrderedJwtClaimsPropagationFilter implements GatewayFilter, Ordered {

        @Override
        public reactor.core.publisher.Mono<Void> filter(
                org.springframework.web.server.ServerWebExchange exchange,
                org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

            // Unconditionally strip all identity headers to prevent client spoofing,
            // regardless of whether JWT authentication is present.
            ServerHttpRequest sanitizedRequest =
                    exchange.getRequest()
                            .mutate()
                            .headers(
                                    h -> {
                                        h.remove(HEADER_USER_ID);
                                        h.remove(HEADER_USER_NAME);
                                        h.remove(HEADER_USER_ROLES);
                                        h.remove(HEADER_USER_PERMISSIONS);
                                    })
                            .build();
            org.springframework.web.server.ServerWebExchange sanitizedExchange =
                    exchange.mutate().request(sanitizedRequest).build();

            return ReactiveSecurityContextHolder.getContext()
                    .filter(ctx -> ctx.getAuthentication() instanceof JwtAuthenticationToken)
                    .map(ctx -> (JwtAuthenticationToken) ctx.getAuthentication())
                    .map(
                            auth -> {
                                Jwt jwt = auth.getToken();
                                ServerHttpRequest.Builder requestBuilder =
                                        sanitizedExchange.getRequest().mutate();

                                // Strip all identity headers to prevent client spoofing
                                requestBuilder.headers(
                                        h -> {
                                            h.remove(HEADER_USER_ID);
                                            h.remove(HEADER_USER_NAME);
                                            h.remove(HEADER_USER_ROLES);
                                            h.remove(HEADER_USER_PERMISSIONS);
                                        });

                                String userId = jwt.getClaimAsString("userId");
                                if (userId != null) {
                                    requestBuilder.header(HEADER_USER_ID, userId);
                                }

                                String subject = jwt.getSubject();
                                if (subject != null) {
                                    requestBuilder.header(HEADER_USER_NAME, subject);
                                }

                                java.util.List<String> roles = jwt.getClaimAsStringList("roles");
                                if (roles != null && !roles.isEmpty()) {
                                    requestBuilder.header(
                                            HEADER_USER_ROLES, String.join(",", roles));
                                }

                                java.util.List<String> permissions =
                                        jwt.getClaimAsStringList("permissions");
                                if (permissions != null && !permissions.isEmpty()) {
                                    requestBuilder.header(
                                            HEADER_USER_PERMISSIONS, String.join(",", permissions));
                                }

                                LOG.debug(
                                        "Propagated JWT claims for user '{}' to downstream headers",
                                        subject);
                                return sanitizedExchange
                                        .mutate()
                                        .request(requestBuilder.build())
                                        .build();
                            })
                    .defaultIfEmpty(sanitizedExchange)
                    .flatMap(chain::filter);
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE + 1;
        }
    }

    /** Configuration holder (currently empty — no user-configurable options). */
    public static class Config {}
}
