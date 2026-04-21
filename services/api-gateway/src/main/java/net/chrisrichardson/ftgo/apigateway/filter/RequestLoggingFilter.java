package net.chrisrichardson.ftgo.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Structured access log for gateway traffic.
 *
 * <p>Emits an {@code INFO} line per incoming request and a second line per
 * response carrying status code and elapsed latency. The correlation ID set
 * by {@link CorrelationIdFilter} is pulled back from the exchange so log
 * lines can be stitched across the gateway and downstream services.
 *
 * <p>Runs just after {@link CorrelationIdFilter} so the ID is available.
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String START_TIME_ATTR = "ftgo.gateway.startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = exchange.getAttributeOrDefault(CorrelationIdFilter.MDC_KEY, "");

        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());
        log.info("gateway request method={} path={} correlationId={}",
                request.getMethod(), request.getURI().getRawPath(), correlationId);

        return chain.filter(exchange).doFinally(signal -> {
            Long start = exchange.getAttribute(START_TIME_ATTR);
            long elapsedMs = start == null ? -1L : System.currentTimeMillis() - start;
            ServerHttpResponse response = exchange.getResponse();
            log.info("gateway response method={} path={} status={} elapsedMs={} correlationId={}",
                    request.getMethod(),
                    request.getURI().getRawPath(),
                    response.getStatusCode() == null ? "unknown" : response.getStatusCode().value(),
                    elapsedMs,
                    correlationId);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
