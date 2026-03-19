package com.ftgo.common.logging.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that enriches MDC with request context (method, URI, remote address)
 * and logs request/response summaries with timing information.
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLoggingMdcFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingMdcFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            MDC.put("httpMethod", request.getMethod());
            MDC.put("requestUri", request.getRequestURI());
            MDC.put("remoteAddr", request.getRemoteAddr());

            log.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("responseStatus", String.valueOf(response.getStatus()));
            MDC.put("durationMs", String.valueOf(duration));

            log.info("Completed request: {} {} status={} duration={}ms",
                    request.getMethod(), request.getRequestURI(),
                    response.getStatus(), duration);

            MDC.remove("httpMethod");
            MDC.remove("requestUri");
            MDC.remove("remoteAddr");
            MDC.remove("responseStatus");
            MDC.remove("durationMs");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/");
    }
}
