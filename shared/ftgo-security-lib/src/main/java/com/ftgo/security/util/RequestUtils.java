package com.ftgo.security.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * Utility class for extracting security-relevant information from HTTP requests.
 */
public final class RequestUtils {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private RequestUtils() {
        // Utility class — prevent instantiation
    }

    /**
     * Extracts a Bearer token from the Authorization header, if present.
     *
     * @param request the HTTP request
     * @return the token string without the "Bearer " prefix, or empty
     */
    public static Optional<String> extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            if (!token.isEmpty()) {
                return Optional.of(token);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the client IP address, accounting for reverse proxies
     * via the {@code X-Forwarded-For} header.
     *
     * @param request the HTTP request
     * @return the client IP address
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
