package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.errors.EntityNotFoundException;
import net.chrisrichardson.ftgo.common.errors.ErrorCode;

/**
 * Order-service-local view of "restaurant missing" — thrown when a
 * {@code createOrder} call references a restaurant id that does not exist
 * in this service's cache/replica. Mapped to HTTP 404 with
 * {@code FTGO-RES-001}.
 */
public class RestaurantNotFoundException extends EntityNotFoundException {
  public RestaurantNotFoundException(long restaurantId) {
    super(ErrorCode.RESTAURANT_NOT_FOUND, "Restaurant not found with id " + restaurantId);
  }
}
