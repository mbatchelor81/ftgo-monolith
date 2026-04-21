package net.chrisrichardson.ftgo.common;

import net.chrisrichardson.ftgo.common.errors.ConflictException;
import net.chrisrichardson.ftgo.common.errors.ErrorCode;

/**
 * Thrown by aggregate state machines (Order, Courier, …) when a requested
 * transition is not permitted from the current state.
 *
 * <p>Mapped to HTTP 409 Conflict with {@code FTGO-ORD-002} via the shared
 * {@code GlobalExceptionHandler}. The offending state is preserved in the
 * exception message for server-side debugging but is NOT exposed in the
 * generic error response body — handlers surface only the stable error
 * code + canonical message.
 */
public class UnsupportedStateTransitionException extends ConflictException {
  public UnsupportedStateTransitionException(Enum state) {
    super(ErrorCode.ORDER_STATE_INVALID, "current state: " + state);
  }
}
