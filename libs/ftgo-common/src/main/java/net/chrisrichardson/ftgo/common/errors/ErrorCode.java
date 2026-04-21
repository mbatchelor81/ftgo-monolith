package net.chrisrichardson.ftgo.common.errors;

/**
 * Canonical catalog of FTGO error codes returned in every {@link ErrorResponse}.
 *
 * <p>Codes are stable strings of the form {@code FTGO-<DOMAIN>-<NNN>}; the
 * numeric suffix is sequential within each domain and MUST NOT be reused when
 * a code is retired. See {@code docs/error-code-catalog.md} for the complete
 * description of every code, including which exception types trigger it and
 * how clients are expected to react.
 */
public enum ErrorCode {

  // ----- Cross-cutting (FTGO-GEN-*) ---------------------------------------
  VALIDATION_FAILED("FTGO-GEN-001", "Request validation failed"),
  MESSAGE_NOT_READABLE("FTGO-GEN-002", "Request body could not be parsed"),
  MISSING_PARAMETER("FTGO-GEN-003", "A required request parameter is missing"),
  TYPE_MISMATCH("FTGO-GEN-004", "A request parameter has the wrong type"),
  METHOD_NOT_ALLOWED("FTGO-GEN-005", "HTTP method not supported on this resource"),
  UNAUTHORIZED("FTGO-GEN-006", "Authentication is required to access this resource"),
  FORBIDDEN("FTGO-GEN-007", "Caller is not permitted to perform this action"),
  CONFLICT("FTGO-GEN-008", "Request conflicts with the current state of the resource"),
  SERVICE_UNAVAILABLE("FTGO-GEN-009", "A downstream dependency is unavailable"),
  INTERNAL_ERROR("FTGO-GEN-999", "Unexpected server error"),

  // ----- Order domain (FTGO-ORD-*) ----------------------------------------
  ORDER_NOT_FOUND("FTGO-ORD-001", "Order not found"),
  ORDER_STATE_INVALID("FTGO-ORD-002", "Order is not in a state that permits the requested action"),
  ORDER_MINIMUM_NOT_MET("FTGO-ORD-003", "Order total is below the minimum required amount"),
  INVALID_MENU_ITEM("FTGO-ORD-004", "Menu item id does not belong to the restaurant"),

  // ----- Consumer domain (FTGO-CON-*) -------------------------------------
  CONSUMER_NOT_FOUND("FTGO-CON-001", "Consumer not found"),

  // ----- Restaurant domain (FTGO-RES-*) -----------------------------------
  RESTAURANT_NOT_FOUND("FTGO-RES-001", "Restaurant not found"),

  // ----- Courier domain (FTGO-CRR-*) --------------------------------------
  COURIER_NOT_FOUND("FTGO-CRR-001", "Courier not found");

  private final String code;
  private final String defaultMessage;

  ErrorCode(String code, String defaultMessage) {
    this.code = code;
    this.defaultMessage = defaultMessage;
  }

  public String code() {
    return code;
  }

  public String defaultMessage() {
    return defaultMessage;
  }
}
