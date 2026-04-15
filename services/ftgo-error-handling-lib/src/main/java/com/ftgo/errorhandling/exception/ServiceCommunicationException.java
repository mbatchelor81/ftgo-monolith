package com.ftgo.errorhandling.exception;

import com.ftgo.errorhandling.model.ErrorCode;

/**
 * Thrown when communication with a downstream service fails.
 *
 * <p>Mapped to HTTP 502 by {@link com.ftgo.errorhandling.handler.GlobalExceptionHandler}.
 */
public class ServiceCommunicationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String serviceName;

    public ServiceCommunicationException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
        this.errorCode = ErrorCode.SERVICE_COMMUNICATION_FAILURE;
    }

    public ServiceCommunicationException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.errorCode = ErrorCode.SERVICE_COMMUNICATION_FAILURE;
    }

    public ServiceCommunicationException(
            ErrorCode errorCode, String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getServiceName() {
        return serviceName;
    }
}
