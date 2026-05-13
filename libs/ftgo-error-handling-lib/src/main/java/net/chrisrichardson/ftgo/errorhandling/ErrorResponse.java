package net.chrisrichardson.ftgo.errorhandling;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Standardized error response DTO returned by all FTGO platform services.
 *
 * <pre>
 * {
 *   "code": "VALIDATION_ERROR",
 *   "message": "One or more fields failed validation",
 *   "details": [ { "field": "quantity", "message": "must be greater than 0" } ],
 *   "timestamp": "2024-06-15T10:30:00Z",
 *   "traceId": "abc123def456"
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String code;
    private final String message;
    private final List<ValidationErrorDetail> details;
    private final Instant timestamp;
    private final String traceId;

    private ErrorResponse(String code, String message, List<ValidationErrorDetail> details,
                          Instant timestamp, String traceId) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
        this.traceId = traceId;
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String traceId) {
        return new ErrorResponse(
                errorCode.name(),
                message != null ? message : errorCode.getDefaultMessage(),
                null,
                Instant.now(),
                traceId
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String traceId) {
        return of(errorCode, errorCode.getDefaultMessage(), traceId);
    }

    public static ErrorResponse withValidationErrors(String message,
                                                     List<ValidationErrorDetail> details,
                                                     String traceId) {
        return new ErrorResponse(
                ErrorCode.VALIDATION_ERROR.name(),
                message,
                details != null ? Collections.unmodifiableList(details) : null,
                Instant.now(),
                traceId
        );
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<ValidationErrorDetail> getDetails() {
        return details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getTraceId() {
        return traceId;
    }
}
