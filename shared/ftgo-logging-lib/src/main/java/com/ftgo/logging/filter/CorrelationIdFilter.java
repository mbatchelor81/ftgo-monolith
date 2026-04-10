package com.ftgo.logging.filter;

import com.ftgo.logging.mdc.LogContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that propagates correlation IDs and request context through
 * the request lifecycle via SLF4J MDC.
 *
 * <p>Extracts the following headers from incoming requests (typically set by
 * the API Gateway) and places them into the MDC:
 * <ul>
 *   <li>{@code X-Correlation-ID} — cross-service correlation identifier (auto-generated if absent)</li>
 *   <li>{@code X-User-ID} — authenticated user identifier</li>
 * </ul>
 *
 * <p>Additionally generates a unique {@code requestId} per request and records
 * the HTTP method and URI path.
 *
 * <p>MDC keys populated:
 * <ul>
 *   <li>{@code correlationId} — unique request correlation identifier</li>
 *   <li>{@code requestId} — unique per-request identifier</li>
 *   <li>{@code userId} — authenticated user ID (from {@code X-User-ID} header)</li>
 *   <li>{@code requestMethod} — HTTP method (GET, POST, etc.)</li>
 *   <li>{@code requestUri} — request URI path</li>
 * </ul>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String USER_ID_HEADER = "X-User-ID";
    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_REQUEST_METHOD = "requestMethod";
    public static final String MDC_REQUEST_URI = "requestUri";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        String requestId = UUID.randomUUID().toString();

        String userId = request.getHeader(USER_ID_HEADER);

        MDC.put(MDC_CORRELATION_ID, correlationId);
        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_REQUEST_METHOD, request.getMethod());
        MDC.put(MDC_REQUEST_URI, request.getRequestURI());

        if (userId != null && !userId.isBlank()) {
            MDC.put(MDC_USER_ID, userId);
        }

        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_USER_ID);
            MDC.remove(MDC_REQUEST_METHOD);
            MDC.remove(MDC_REQUEST_URI);
        }
    }
}
