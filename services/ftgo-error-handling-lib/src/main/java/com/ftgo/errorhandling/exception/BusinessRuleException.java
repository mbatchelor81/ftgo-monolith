package com.ftgo.errorhandling.exception;

import com.ftgo.errorhandling.model.ErrorCode;

/**
 * Thrown when a business rule prevents an operation from completing.
 *
 * <p>Mapped to HTTP 422 by {@link com.ftgo.errorhandling.handler.GlobalExceptionHandler}.
 */
public class BusinessRuleException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessRuleException(String message) {
        super(message);
        this.errorCode = ErrorCode.BUSINESS_RULE_VIOLATION;
    }

    public BusinessRuleException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
