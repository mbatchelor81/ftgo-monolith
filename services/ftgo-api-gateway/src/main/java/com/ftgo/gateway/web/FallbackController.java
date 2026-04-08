package com.ftgo.gateway.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Fallback controller invoked when a circuit breaker trips.
 *
 * <p>Returns a standardised 503 response so consumers know the
 * downstream service is temporarily unavailable. Uses
 * {@code @RequestMapping} (all methods) so that POST, PUT, DELETE
 * requests also receive a proper 503 instead of 405.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @RequestMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, String>> ordersFallback(ServerWebExchange exchange) {
        return fallbackResponse("Order Service", exchange);
    }

    @RequestMapping(value = "/consumers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, String>> consumersFallback(ServerWebExchange exchange) {
        return fallbackResponse("Consumer Service", exchange);
    }

    @RequestMapping(value = "/restaurants", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, String>> restaurantsFallback(ServerWebExchange exchange) {
        return fallbackResponse("Restaurant Service", exchange);
    }

    @RequestMapping(value = "/couriers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, String>> couriersFallback(ServerWebExchange exchange) {
        return fallbackResponse("Courier Service", exchange);
    }

    private Mono<Map<String, String>> fallbackResponse(String serviceName, ServerWebExchange exchange) {
        log.warn("Circuit breaker fallback triggered for {}", serviceName);
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        return Mono.just(Map.of(
                "status", "SERVICE_UNAVAILABLE",
                "message", serviceName + " is temporarily unavailable. Please try again later."
        ));
    }
}
