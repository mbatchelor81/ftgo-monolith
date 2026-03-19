package com.ftgo.apigateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Fallback controller for circuit breaker open states.
 *
 * <p>When a downstream service is unavailable and the circuit breaker
 * is open, requests are forwarded here instead of timing out.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/service-unavailable")
    public Mono<ResponseEntity<Map<String, Object>>> serviceUnavailable() {
        Map<String, Object> body = Map.of(
                "errorCode", "FTGO-200",
                "message", "Service is temporarily unavailable. Please try again later.",
                "status", 503,
                "timestamp", Instant.now().toString()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body));
    }
}
