package com.ftgo.observability.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that populates MDC with request-scoped context for structured logging.
 *
 * <p>Adds the service name, request method, and request URI to MDC so that every log statement
 * within the request scope includes this context. This is especially useful for JSON-formatted logs
 * consumed by Fluentd/Elasticsearch.
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 11)
public class ServiceMdcFilter extends OncePerRequestFilter {

    public static final String SERVICE_MDC_KEY = "service";
    public static final String REQUEST_METHOD_MDC_KEY = "requestMethod";
    public static final String REQUEST_URI_MDC_KEY = "requestUri";

    private final String serviceName;

    public ServiceMdcFilter(@Value("${spring.application.name:unknown}") String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        MDC.put(SERVICE_MDC_KEY, serviceName);
        MDC.put(REQUEST_METHOD_MDC_KEY, request.getMethod());
        MDC.put(REQUEST_URI_MDC_KEY, request.getRequestURI());

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(SERVICE_MDC_KEY);
            MDC.remove(REQUEST_METHOD_MDC_KEY);
            MDC.remove(REQUEST_URI_MDC_KEY);
        }
    }
}
