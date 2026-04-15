package com.ftgo.apigateway.circuitbreaker;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fallback controller invoked when a downstream service circuit breaker is open.
 *
 * <p>Returns a structured JSON error response with HTTP 503 (Service Unavailable), signalling to
 * clients that the target service is temporarily unavailable and they should retry later.
 */
@RestController
public class FallbackController {

    @RequestMapping("/fallback")
    public ResponseEntity<Map<String, Object>> fallback() {
        Map<String, Object> body =
                Map.of(
                        "status",
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "error",
                        "Service Unavailable",
                        "message",
                        "The requested service is temporarily unavailable. "
                                + "Please try again later.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
