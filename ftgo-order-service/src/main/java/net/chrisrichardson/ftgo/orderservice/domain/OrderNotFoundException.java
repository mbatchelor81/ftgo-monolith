package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.errors.EntityNotFoundException;
import net.chrisrichardson.ftgo.common.errors.ErrorCode;

/**
 * Signals that an order lookup found no matching row. Extending
 * {@link EntityNotFoundException} wires this exception into the shared
 * {@code GlobalExceptionHandler} so it is returned as HTTP 404 with the
 * stable {@code FTGO-ORD-001} error code.
 */
public class OrderNotFoundException extends EntityNotFoundException {
  public OrderNotFoundException(Long orderId) {
    super(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderId);
  }
}
