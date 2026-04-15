package com.ftgo.apigateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

/** Unit tests for {@link CorrelationIdGatewayFilterFactory}. */
class CorrelationIdGatewayFilterFactoryTest {

    private final CorrelationIdGatewayFilterFactory factory =
            new CorrelationIdGatewayFilterFactory();

    @Test
    void apply_returnsNonNullFilter() {
        GatewayFilter filter = factory.apply(new CorrelationIdGatewayFilterFactory.Config());
        assertThat(filter).isNotNull();
    }

    @Test
    void existingCorrelationId_isPreserved() {
        String existingId = "existing-correlation-id-123";
        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/orders")
                        .header(CorrelationIdGatewayFilterFactory.CORRELATION_ID_HEADER, existingId)
                        .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilter filter = factory.apply(new CorrelationIdGatewayFilterFactory.Config());

        filter.filter(
                exchange,
                ex -> {
                    String headerValue =
                            ex.getRequest()
                                    .getHeaders()
                                    .getFirst(
                                            CorrelationIdGatewayFilterFactory
                                                    .CORRELATION_ID_HEADER);
                    assertThat(headerValue).isEqualTo(existingId);
                    return ex.getResponse().setComplete();
                });
    }

    @Test
    void missingCorrelationId_isGenerated() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilter filter = factory.apply(new CorrelationIdGatewayFilterFactory.Config());

        filter.filter(
                exchange,
                ex -> {
                    String headerValue =
                            ex.getRequest()
                                    .getHeaders()
                                    .getFirst(
                                            CorrelationIdGatewayFilterFactory
                                                    .CORRELATION_ID_HEADER);
                    assertThat(headerValue).isNotNull().isNotBlank();
                    return ex.getResponse().setComplete();
                });
    }

    @Test
    void correlationId_isAddedToResponseHeaders() {
        String existingId = "response-correlation-123";
        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/orders")
                        .header(CorrelationIdGatewayFilterFactory.CORRELATION_ID_HEADER, existingId)
                        .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilter filter = factory.apply(new CorrelationIdGatewayFilterFactory.Config());

        filter.filter(exchange, ex -> ex.getResponse().setComplete()).block();

        assertThat(
                        exchange.getResponse()
                                .getHeaders()
                                .getFirst(CorrelationIdGatewayFilterFactory.CORRELATION_ID_HEADER))
                .isEqualTo(existingId);
    }

    @Test
    void correlationId_isStoredInReactorContext() {
        String existingId = "ctx-correlation-456";
        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/orders")
                        .header(CorrelationIdGatewayFilterFactory.CORRELATION_ID_HEADER, existingId)
                        .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilter filter = factory.apply(new CorrelationIdGatewayFilterFactory.Config());

        AtomicReference<String> contextValue = new AtomicReference<>();

        filter.filter(
                        exchange,
                        ex ->
                                Mono.deferContextual(
                                        ctx -> {
                                            contextValue.set(
                                                    ctx.getOrDefault(
                                                            CorrelationIdGatewayFilterFactory
                                                                    .MDC_CORRELATION_ID,
                                                            null));
                                            return ex.getResponse().setComplete();
                                        }))
                .block();

        assertThat(contextValue.get()).isEqualTo(existingId);
    }
}
