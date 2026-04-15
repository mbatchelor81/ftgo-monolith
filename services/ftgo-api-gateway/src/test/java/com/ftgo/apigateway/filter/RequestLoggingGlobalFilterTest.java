package com.ftgo.apigateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

/** Unit tests for {@link RequestLoggingGlobalFilter}. */
class RequestLoggingGlobalFilterTest {

    private final RequestLoggingGlobalFilter filter = new RequestLoggingGlobalFilter();

    @Test
    void filter_completesSuccessfully() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, ex -> ex.getResponse().setComplete());

        result.block();
        // No exception thrown — logging completed successfully
    }

    @Test
    void order_isAfterCorrelationIdFilter() {
        assertThat(filter.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 10);
    }
}
