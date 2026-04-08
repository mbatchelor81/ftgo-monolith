package com.ftgo.security.filter;

import com.ftgo.security.util.RequestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that logs security-relevant request metadata for auditing.
 *
 * <p>Enabled by default; disable with
 * {@code ftgo.security.logging.enabled=false}.
 *
 * <p>Logs the HTTP method, URI, client IP, and response status at DEBUG level.
 */
@Order(Integer.MIN_VALUE + 100)
public class SecurityLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SecurityLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String clientIp = RequestUtils.getClientIpAddress(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();

        log.debug("Security request: method={}, uri={}, clientIp={}", method, uri, clientIp);

        try {
            filterChain.doFilter(request, response);
        } finally {
            log.debug("Security response: method={}, uri={}, status={}, clientIp={}",
                method, uri, response.getStatus(), clientIp);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip logging for actuator health checks to reduce noise
        String uri = request.getRequestURI();
        return uri.equals("/actuator/health") || uri.equals("/actuator/health/liveness")
            || uri.equals("/actuator/health/readiness");
    }
}
