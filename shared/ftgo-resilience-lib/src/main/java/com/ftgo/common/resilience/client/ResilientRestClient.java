package com.ftgo.common.resilience.client;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Base class for resilient inter-service REST clients.
 *
 * <p>Subclass this for each downstream dependency, providing the
 * service name used as the Resilience4j instance key. The circuit
 * breaker, retry, and bulkhead annotations are inherited by
 * subclass methods that call {@link #get}, {@link #post}, etc.
 */
public abstract class ResilientRestClient {

    private static final Logger log = LoggerFactory.getLogger(ResilientRestClient.class);

    protected final RestTemplate restTemplate;
    protected final String baseUrl;
    protected final String serviceName;

    protected ResilientRestClient(RestTemplate restTemplate, String baseUrl, String serviceName) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.serviceName = serviceName;
    }

    @CircuitBreaker(name = "#{serviceName}", fallbackMethod = "fallbackGet")
    @Retry(name = "#{serviceName}")
    @Bulkhead(name = "#{serviceName}")
    protected <T> ResponseEntity<T> get(String path, Class<T> responseType) {
        String url = baseUrl + path;
        log.debug("GET {} (service={})", url, serviceName);
        return restTemplate.getForEntity(url, responseType);
    }

    @CircuitBreaker(name = "#{serviceName}", fallbackMethod = "fallbackPost")
    @Retry(name = "#{serviceName}")
    @Bulkhead(name = "#{serviceName}")
    protected <T> ResponseEntity<T> post(String path, Object body, Class<T> responseType) {
        String url = baseUrl + path;
        log.debug("POST {} (service={})", url, serviceName);
        return restTemplate.postForEntity(url, body, responseType);
    }

    protected <T> ResponseEntity<T> fallbackGet(String path, Class<T> responseType, Throwable t) {
        log.warn("Circuit breaker fallback for GET {} (service={}): {}", baseUrl + path, serviceName, t.getMessage());
        throw new ServiceUnavailableException(serviceName, t);
    }

    protected <T> ResponseEntity<T> fallbackPost(String path, Object body, Class<T> responseType, Throwable t) {
        log.warn("Circuit breaker fallback for POST {} (service={}): {}", baseUrl + path, serviceName, t.getMessage());
        throw new ServiceUnavailableException(serviceName, t);
    }

    /**
     * Exception thrown when a downstream service is unavailable
     * and the circuit breaker fallback is triggered.
     */
    public static class ServiceUnavailableException extends RuntimeException {
        private final String serviceName;

        public ServiceUnavailableException(String serviceName, Throwable cause) {
            super("Service unavailable: " + serviceName, cause);
            this.serviceName = serviceName;
        }

        public String getServiceName() {
            return serviceName;
        }
    }
}
