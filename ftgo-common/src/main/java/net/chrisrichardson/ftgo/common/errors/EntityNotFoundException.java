package net.chrisrichardson.ftgo.common.errors;

/**
 * Thrown when a request references a domain entity that does not exist.
 *
 * <p>Handled by {@code GlobalExceptionHandler} as HTTP 404. Subclasses (e.g.
 * {@code OrderNotFoundException}) supply a more specific {@link ErrorCode}
 * so clients can distinguish between e.g. an unknown order and an unknown
 * restaurant without string-matching the message.
 */
public class EntityNotFoundException extends FtgoException {

  public EntityNotFoundException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }

  public EntityNotFoundException(ErrorCode errorCode, String message, Throwable cause) {
    super(errorCode, message, cause);
  }
}
