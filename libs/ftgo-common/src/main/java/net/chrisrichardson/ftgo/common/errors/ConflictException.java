package net.chrisrichardson.ftgo.common.errors;

/**
 * Thrown when a request cannot be satisfied because it conflicts with the
 * current state of the target resource (HTTP 409).
 *
 * <p>Used for concurrency conflicts and illegal state transitions (e.g.
 * accepting an order that has already been cancelled).
 */
public class ConflictException extends FtgoException {

  public ConflictException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }

  public ConflictException(ErrorCode errorCode, String message, Throwable cause) {
    super(errorCode, message, cause);
  }
}
