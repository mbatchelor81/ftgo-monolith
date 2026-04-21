package net.chrisrichardson.ftgo.common.errors;

/**
 * Base class for every FTGO domain exception that carries an {@link ErrorCode}.
 *
 * <p>{@code GlobalExceptionHandler} inspects this type to map the exception
 * onto an {@link ErrorResponse} without needing a dedicated handler per
 * subclass. Subclasses pick the appropriate HTTP status by extending one of
 * the more specific types in this package (e.g. {@link EntityNotFoundException},
 * {@link ConflictException}, {@link BusinessRuleViolationException}).
 */
public abstract class FtgoException extends RuntimeException {

  private final ErrorCode errorCode;

  protected FtgoException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  protected FtgoException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
