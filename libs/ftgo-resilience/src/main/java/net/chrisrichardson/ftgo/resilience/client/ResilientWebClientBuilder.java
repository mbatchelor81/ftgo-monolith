package net.chrisrichardson.ftgo.resilience.client;

import java.time.Duration;
import java.util.Objects;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import reactor.core.publisher.Mono;

/**
 * Produces {@link WebClient} instances that compose a Resilience4j
 * circuit breaker, retry (with exponential backoff configured via the
 * per-instance Resilience4j settings), and bulkhead for thread-pool
 * isolation onto every reactive exchange.
 *
 * <p>Services obtain a builder by injecting
 * {@code ResilientWebClientBuilder} and calling
 * {@link #builder(String)} with the logical name of the downstream
 * service — the name is used both to look up the configured base URL and
 * to select the matching Resilience4j instance (so operators can tune
 * latency budgets per dependency).
 */
public class ResilientWebClientBuilder {

    private final Builder webClientBuilder;
    private final ServiceEndpoints endpoints;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final BulkheadRegistry bulkheadRegistry;
    private final String defaultInstanceName;

    public ResilientWebClientBuilder(Builder webClientBuilder,
                                     ServiceEndpoints endpoints,
                                     CircuitBreakerRegistry circuitBreakerRegistry,
                                     RetryRegistry retryRegistry,
                                     BulkheadRegistry bulkheadRegistry,
                                     String defaultInstanceName) {
        this.webClientBuilder = Objects.requireNonNull(webClientBuilder, "webClientBuilder");
        this.endpoints = Objects.requireNonNull(endpoints, "endpoints");
        this.circuitBreakerRegistry = Objects.requireNonNull(circuitBreakerRegistry, "circuitBreakerRegistry");
        this.retryRegistry = Objects.requireNonNull(retryRegistry, "retryRegistry");
        this.bulkheadRegistry = Objects.requireNonNull(bulkheadRegistry, "bulkheadRegistry");
        this.defaultInstanceName = Objects.requireNonNull(defaultInstanceName, "defaultInstanceName");
    }

    /**
     * @param serviceName logical service name — must be present in
     *     {@code ftgo.services.*} and, ideally, in
     *     {@code resilience4j.*.instances.*}. Missing instances fall back
     *     to the configured default.
     */
    public WebClient build(String serviceName) {
        String baseUrl = endpoints.baseUrl(serviceName);
        return webClientBuilder
                .baseUrl(baseUrl)
                .filter((request, next) -> next.exchange(request)
                        .transformDeferred(applyResilience(serviceName)))
                .build();
    }

    /**
     * @return a reusable {@code Function<Mono<T>, Mono<T>>}-style operator
     *     that layers circuit breaker → retry → bulkhead onto any
     *     reactive exchange. Exposed so callers with more exotic client
     *     wiring (non-{@code WebClient} reactive producers) can still
     *     reuse the same policy stack.
     */
    public <T> java.util.function.Function<Mono<T>, Mono<T>> applyResilience(String serviceName) {
        String instance = resolveInstance(serviceName);
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(instance);
        Retry retry = retryRegistry.retry(instance);
        return mono -> mono
                .transformDeferred(BulkheadOperator.of(bulkheadRegistry.bulkhead(instance)))
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
    }

    /**
     * @return the service name itself if a Resilience4j instance of that
     *     name exists; otherwise the configured default ({@code default})
     *     so callers always get a well-tuned stack.
     */
    private String resolveInstance(String serviceName) {
        if (circuitBreakerRegistry.find(serviceName).isPresent()) {
            return serviceName;
        }
        return defaultInstanceName;
    }

    /**
     * Exposed primarily for tests that want to assert the default
     * connection/read timeout used by generated clients. Services that
     * need bespoke timeouts should configure them via Resilience4j
     * {@code timelimiter} instances rather than changing the WebClient.
     */
    public Duration defaultTimeout() {
        return Duration.ofSeconds(10);
    }
}
