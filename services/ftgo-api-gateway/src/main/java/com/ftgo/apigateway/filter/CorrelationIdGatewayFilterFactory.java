package com.ftgo.apigateway.filter;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;

/**
 * Gateway filter that ensures every request carries a correlation ID.
 *
 * <p>If the incoming request already contains an {@code X-Correlation-Id} header, it is preserved.
 * Otherwise a new UUID is generated. The correlation ID is:
 *
 * <ul>
 *   <li>Added to the request headers forwarded to downstream services
 *   <li>Added to the response headers returned to the caller
 *   <li>Stored in Reactor {@link Context} and propagated to SLF4J MDC on each signal
 * </ul>
 */
@Component
public class CorrelationIdGatewayFilterFactory
        extends AbstractGatewayFilterFactory<CorrelationIdGatewayFilterFactory.Config> {

    private static final Logger LOG =
            LoggerFactory.getLogger(CorrelationIdGatewayFilterFactory.class);

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    static final String MDC_CORRELATION_ID = "correlationId";

    public CorrelationIdGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedCorrelationIdFilter();
    }

    private static final class OrderedCorrelationIdFilter implements GatewayFilter, Ordered {

        @Override
        public reactor.core.publisher.Mono<Void> filter(
                org.springframework.web.server.ServerWebExchange exchange,
                org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

            String correlationId =
                    exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            final String finalCorrelationId = correlationId;

            ServerHttpRequest mutatedRequest =
                    exchange.getRequest()
                            .mutate()
                            .header(CORRELATION_ID_HEADER, finalCorrelationId)
                            .build();

            exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, finalCorrelationId);

            return chain.filter(exchange.mutate().request(mutatedRequest).build())
                    .doOnEach(
                            signal -> {
                                if (!signal.isOnComplete()
                                        && !signal.isOnError()
                                        && signal.getType() != SignalType.ON_NEXT) {
                                    return;
                                }
                                String ctxId =
                                        signal.getContextView()
                                                .getOrDefault(MDC_CORRELATION_ID, null);
                                if (ctxId != null) {
                                    MDC.put(MDC_CORRELATION_ID, ctxId);
                                }
                            })
                    .doFinally(signalType -> MDC.remove(MDC_CORRELATION_ID))
                    .contextWrite(Context.of(MDC_CORRELATION_ID, finalCorrelationId));
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }

    /** Configuration holder (currently empty — no user-configurable options). */
    public static class Config {}
}
