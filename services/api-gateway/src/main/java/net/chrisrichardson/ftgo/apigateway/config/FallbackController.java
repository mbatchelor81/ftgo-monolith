package net.chrisrichardson.ftgo.apigateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Static fallbacks returned when a downstream microservice's Resilience4j
 * circuit breaker trips. Keeps the response shape consistent so clients can
 * branch on HTTP 503 + JSON body rather than a raw connection failure.
 */
@RestController
public class FallbackController {

    @RequestMapping(
            value = "/fallback/{service}",
            method = {
                    RequestMethod.GET,
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE
            },
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> fallback(@PathVariable String service) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "service_unavailable",
                        "service", service,
                        "message", "Downstream service is unavailable. Please retry shortly."
                ));
    }
}
