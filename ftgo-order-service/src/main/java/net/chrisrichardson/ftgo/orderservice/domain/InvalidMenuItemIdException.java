package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.errors.BusinessRuleViolationException;
import net.chrisrichardson.ftgo.common.errors.ErrorCode;

/**
 * Thrown when {@code createOrder} references a menu item id that does not
 * exist on the target restaurant. Well-formed request, known restaurant,
 * but the item id is wrong — mapped to HTTP 422 with {@code FTGO-ORD-004}.
 */
public class InvalidMenuItemIdException extends BusinessRuleViolationException {
  public InvalidMenuItemIdException(String menuItemId) {
    super(ErrorCode.INVALID_MENU_ITEM, "Invalid menu item id " + menuItemId);
  }
}
