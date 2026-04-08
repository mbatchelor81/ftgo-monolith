package com.ftgo.logging.filter;

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
 * Servlet filter that propagates correlation IDs through the request lifecycle.
 *
 * <p>Extracts the {@code X-Correlation-ID} header from incoming requests (typically
 * set by the API Gateway) and places it into the SLF4J MDC. If no correlation ID
 * header is present, a new UUID is generated. The correlation ID is also set as a
 * response header for downstream tracing.
 *
 * <p>MDC keys populated:
 * <ul>
 *   <li>{@code correlationId} — unique request correlation identifier</li>
 *   <li>{@code requestMethod} — HTTP method (GET, POST, etc.)</li>
 *   <li>{@code requestUri} — request URI path</li>
 * </ul>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String MDC_CORRELATION_ID = "correlationId";
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

        MDC.put(MDC_CORRELATION_ID, correlationId);
        MDC.put(MDC_REQUEST_METHOD, request.getMethod());
        MDC.put(MDC_REQUEST_URI, request.getRequestURI());

        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_REQUEST_METHOD);
            MDC.remove(MDC_REQUEST_URI);
        }
    }
}
