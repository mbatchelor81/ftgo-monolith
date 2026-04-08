package com.ftgo.errorhandling.exception;

/**
 * Thrown when an inter-service communication failure occurs.
 *
 * <p>This covers scenarios such as:
 * <ul>
 *   <li>Downstream service timeouts</li>
 *   <li>Connection refused / unreachable services</li>
 *   <li>Non-2xx responses from downstream services</li>
 *   <li>Circuit breaker open state</li>
 * </ul>
 *
 * <p>Mapped to HTTP 502 Bad Gateway by the GlobalExceptionHandler.
 */
public class ServiceCommunicationException extends RuntimeException {

    private final String targetService;

    public ServiceCommunicationException(String targetService, String message) {
        super(message);
        this.targetService = targetService;
    }

    public ServiceCommunicationException(String targetService, String message, Throwable cause) {
        super(message, cause);
        this.targetService = targetService;
    }

    public String getTargetService() {
        return targetService;
    }
}
