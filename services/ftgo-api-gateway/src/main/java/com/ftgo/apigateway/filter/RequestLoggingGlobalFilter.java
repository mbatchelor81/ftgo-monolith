package com.ftgo.apigateway.filter;

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
 * Global filter that logs every request and response flowing through the gateway.
 *
 * <p>Logged information includes: HTTP method, path, query parameters, response status code, and
 * elapsed time in milliseconds. The correlation ID (set by {@link
 * CorrelationIdGatewayFilterFactory}) is available in the MDC for log aggregation.
 */
@Component
public class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();

        LOG.info(
                "Gateway request: {} {} query={}",
                request.getMethod(),
                request.getURI().getPath(),
                request.getURI().getQuery());

        return chain.filter(exchange)
                .then(
                        Mono.fromRunnable(
                                () -> {
                                    ServerHttpResponse response = exchange.getResponse();
                                    long elapsed = System.currentTimeMillis() - startTime;
                                    LOG.info(
                                            "Gateway response: {} {} status={} elapsed={}ms",
                                            request.getMethod(),
                                            request.getURI().getPath(),
                                            response.getStatusCode(),
                                            elapsed);
                                }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
