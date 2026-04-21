package net.chrisrichardson.ftgo.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * Extracts (or mints) a correlation ID for every inbound HTTP request and
 * publishes it into SLF4J's MDC so structured log lines include it.
 *
 * <p>Precedence for the inbound value:
 * <ol>
 *   <li>{@code X-Correlation-ID} header</li>
 *   <li>{@code X-Request-ID} header</li>
 *   <li>Freshly-generated UUID</li>
 * </ol>
 *
 * <p>The resolved value is also written to the response as
 * {@code X-Correlation-ID} so calling services can log the same ID.
 */
public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String correlationId = resolveCorrelationId(httpRequest);
        String requestId = UUID.randomUUID().toString();

        MDC.put(MdcKeys.CORRELATION_ID, correlationId);
        MDC.put(MdcKeys.REQUEST_ID, requestId);
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MdcKeys.CORRELATION_ID);
            MDC.remove(MdcKeys.REQUEST_ID);
        }
    }

    private static String resolveCorrelationId(HttpServletRequest request) {
        String value = request.getHeader(CORRELATION_ID_HEADER);
        if (value == null || value.isBlank()) {
            value = request.getHeader(REQUEST_ID_HEADER);
        }
        if (value == null || value.isBlank()) {
            value = UUID.randomUUID().toString();
        }
        return value;
    }
}
