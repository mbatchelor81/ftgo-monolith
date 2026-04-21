package net.chrisrichardson.ftgo.common.errors;

/**
 * Thrown when an inter-service call cannot be completed because a downstream
 * dependency is unreachable, timing out, or returning an error that the
 * caller cannot recover from (HTTP 503).
 *
 * <p>Typical triggers: circuit breaker open, connection refused, read
 * timeout, or an unexpected 5xx response from a sibling service.
 */
public class ServiceUnavailableException extends FtgoException {

  public ServiceUnavailableException(String message) {
    super(ErrorCode.SERVICE_UNAVAILABLE, message);
  }

  public ServiceUnavailableException(String message, Throwable cause) {
    super(ErrorCode.SERVICE_UNAVAILABLE, message, cause);
  }
}
