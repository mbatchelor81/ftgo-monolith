package net.chrisrichardson.ftgo.common.errors;

/**
 * Thrown when a request is well-formed but violates a domain rule that the
 * server can identify before attempting to execute the action (HTTP 422).
 *
 * <p>Classic example: an order that does not meet the restaurant's minimum
 * order total. The request is syntactically valid, the referenced entities
 * exist, but the business rule forbids the action.
 */
public class BusinessRuleViolationException extends FtgoException {

  public BusinessRuleViolationException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }

  public BusinessRuleViolationException(ErrorCode errorCode, String message, Throwable cause) {
    super(errorCode, message, cause);
  }
}
