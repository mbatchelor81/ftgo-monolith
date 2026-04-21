package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.errors.BusinessRuleViolationException;
import net.chrisrichardson.ftgo.common.errors.ErrorCode;

/**
 * Thrown by {@code Consumer.validateOrderByConsumer(Money)} when the
 * proposed order total falls below the per-consumer minimum.
 *
 * <p>Mapped to HTTP 422 Unprocessable Entity with {@code FTGO-ORD-003} by
 * the shared {@code GlobalExceptionHandler}. The request was well-formed
 * and the referenced entities exist, so 400 would be misleading — 422
 * correctly signals "I understood you, but the business rule forbids it."
 */
public class OrderMinimumNotMetException extends BusinessRuleViolationException {
  public OrderMinimumNotMetException() {
    super(ErrorCode.ORDER_MINIMUM_NOT_MET,
        "Order total is below the minimum required amount");
  }
}
