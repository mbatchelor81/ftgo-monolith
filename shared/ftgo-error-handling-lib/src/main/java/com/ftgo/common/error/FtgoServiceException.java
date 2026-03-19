package com.ftgo.common.error;

/**
 * Base exception for all FTGO domain-specific errors.
 *
 * <p>Carries an {@link ErrorCode} that determines the HTTP status code and
 * error code in the response. Subclass this for domain-specific exceptions.
 */
public class FtgoServiceException extends RuntimeException {

    private final ErrorCode errorCode;

    public FtgoServiceException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public FtgoServiceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public FtgoServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
