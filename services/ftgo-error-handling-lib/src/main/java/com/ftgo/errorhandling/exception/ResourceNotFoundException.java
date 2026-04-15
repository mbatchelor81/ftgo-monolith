package com.ftgo.errorhandling.exception;

import com.ftgo.errorhandling.model.ErrorCode;

/**
 * Thrown when a requested entity cannot be found.
 *
 * <p>Mapped to HTTP 404 by {@link com.ftgo.errorhandling.handler.GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCode.RESOURCE_NOT_FOUND;
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
