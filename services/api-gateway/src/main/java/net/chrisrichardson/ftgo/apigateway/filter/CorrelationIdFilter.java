package net.chrisrichardson.ftgo.apigateway.filter;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Ensures every request flowing through the gateway carries a stable
 * {@value #HEADER} header that downstream microservices and log lines can
 * pivot on.
 *
 * <p>If the caller already supplied a correlation ID we honor it; otherwise
 * a fresh UUID is generated. The value is mutated onto the outgoing request
 * and mirrored on the response so clients can surface it in error reports.
 *
 * <p>Runs with the highest precedence so later filters (logging, auth) see
 * the ID in SLF4J's MDC.
 */
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String resolved = correlationId;
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(HEADER, resolved)
                .build();
        exchange.getResponse().getHeaders().set(HEADER, resolved);

        ServerWebExchange mutated = exchange.mutate().request(mutatedRequest).build();
        mutated.getAttributes().put(MDC_KEY, resolved);

        MDC.put(MDC_KEY, resolved);
        return chain.filter(mutated)
                .doFinally(signal -> MDC.remove(MDC_KEY));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
