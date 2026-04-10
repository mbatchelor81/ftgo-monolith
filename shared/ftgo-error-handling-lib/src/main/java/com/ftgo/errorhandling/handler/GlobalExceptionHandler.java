package com.ftgo.errorhandling.handler;

import com.ftgo.errorhandling.dto.ErrorResponse;
import com.ftgo.errorhandling.exception.ErrorCodes;
import com.ftgo.errorhandling.exception.ResourceNotFoundException;
import com.ftgo.errorhandling.exception.ServiceCommunicationException;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import net.chrisrichardson.ftgo.common.NotYetImplementedException;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.domain.OrderMinimumNotMetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for all FTGO microservices.
 *
 * <p>Catches all exceptions thrown by controllers and returns a standardized
 * {@link ErrorResponse} with appropriate HTTP status codes. Integrates with
 * Micrometer Tracing to include the current traceId in every error response
 * for distributed debugging.
 *
 * <p>Exception-to-status mapping:
 * <ul>
 *   <li>{@link MethodArgumentNotValidException} → 400 Bad Request</li>
 *   <li>{@link ConstraintViolationException} → 400 Bad Request</li>
 *   <li>{@link HttpMessageNotReadableException} → 400 Bad Request</li>
 *   <li>{@link ResourceNotFoundException} → 404 Not Found</li>
 *   <li>{@link UnsupportedStateTransitionException} → 409 Conflict</li>
 *   <li>{@link OrderMinimumNotMetException} → 422 Unprocessable Entity</li>
 *   <li>{@link ServiceCommunicationException} → 502 Bad Gateway</li>
 *   <li>{@link RestClientException} → 502 Bad Gateway</li>
 *   <li>{@link Exception} (fallback) → 500 Internal Server Error</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    // =========================================================================
    // Validation errors — 400 Bad Request
    // =========================================================================

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ErrorResponse.FieldError(
                        fe.getField(),
                        fe.getDefaultMessage(),
                        fe.getRejectedValue()))
                .collect(Collectors.toList());

        ErrorResponse body = buildErrorResponse(
                ErrorCodes.VALIDATION_ERROR,
                "Validation failed",
                fieldErrors,
                getRequestPath(request));

        log.warn("Validation failed: {} field error(s) [traceId={}]",
                fieldErrors.size(), body.getTraceId());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(cv -> new ErrorResponse.FieldError(
                        extractFieldName(cv),
                        cv.getMessage(),
                        cv.getInvalidValue()))
                .collect(Collectors.toList());

        ErrorResponse body = buildErrorResponse(
                ErrorCodes.VALIDATION_ERROR,
                "Validation failed",
                fieldErrors,
                request.getRequestURI());

        log.warn("Constraint violation: {} violation(s) [traceId={}]",
                fieldErrors.size(), body.getTraceId());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // =========================================================================
    // Malformed request — 400 Bad Request
    // =========================================================================

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ErrorResponse body = buildErrorResponse(
                ErrorCodes.MALFORMED_REQUEST,
                "Malformed request body",
                null,
                getRequestPath(request));

        log.warn("Malformed request body [traceId={}]: {}", body.getTraceId(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // =========================================================================
    // Method not allowed — 405
    // =========================================================================

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ErrorResponse body = buildErrorResponse(
                ErrorCodes.METHOD_NOT_ALLOWED,
                "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint",
                null,
                getRequestPath(request));

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    // =========================================================================
    // Unsupported media type — 415
    // =========================================================================

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ErrorResponse body = buildErrorResponse(
                ErrorCodes.UNSUPPORTED_MEDIA_TYPE,
                "Content type '" + ex.getContentType() + "' is not supported",
                null,
                getRequestPath(request));

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(body);
    }

    // =========================================================================
    // Resource not found — 404
    // =========================================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse body = buildErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                request.getRequestURI());

        log.warn("Resource not found: {} [traceId={}]", ex.getMessage(), body.getTraceId());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // =========================================================================
    // State conflict — 409 Conflict
    // =========================================================================

    @ExceptionHandler(UnsupportedStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleStateConflict(
            UnsupportedStateTransitionException ex,
            HttpServletRequest request) {

        ErrorResponse body = buildErrorResponse(
                ErrorCodes.STATE_CONFLICT,
                ex.getMessage(),
                null,
                request.getRequestURI());

        log.warn("State transition conflict: {} [traceId={}]", ex.getMessage(), body.getTraceId());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // =========================================================================
    // Business rule violation — 422 Unprocessable Entity
    // =========================================================================

    @ExceptionHandler(OrderMinimumNotMetException.class)
    public ResponseEntity<ErrorResponse> handleOrderMinimumNotMet(
            OrderMinimumNotMetException ex,
            HttpServletRequest request) {

        ErrorResponse body = buildErrorResponse(
                ErrorCodes.ORDER_MINIMUM_NOT_MET,
                "Order total does not meet the restaurant's minimum requirement",
                null,
                request.getRequestURI());

        log.warn("Order minimum not met [traceId={}]", body.getTraceId());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    // =========================================================================
    // Not yet implemented — 501
    // =========================================================================

    @ExceptionHandler(NotYetImplementedException.class)
    public ResponseEntity<ErrorResponse> handleNotYetImplemented(
            NotYetImplementedException ex,
            HttpServletRequest request) {

        ErrorResponse body = buildErrorResponse(
                ErrorCodes.INTERNAL_ERROR,
                "This feature is not yet implemented",
                null,
                request.getRequestURI());

        log.warn("Not yet implemented endpoint called: {} [traceId={}]",
                request.getRequestURI(), body.getTraceId());

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(body);
    }

    // =========================================================================
    // Inter-service communication failure — 502 Bad Gateway
    // =========================================================================

    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<ErrorResponse> handleServiceCommunication(
            ServiceCommunicationException ex,
            HttpServletRequest request) {

        ErrorResponse body = buildErrorResponse(
                ErrorCodes.SERVICE_COMMUNICATION_ERROR,
                "Failed to communicate with downstream service: " + ex.getTargetService(),
                null,
                request.getRequestURI());

        log.error("Service communication failure [target={}, traceId={}]: {}",
                ex.getTargetService(), body.getTraceId(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleResourceAccess(
            ResourceAccessException ex,
            HttpServletRequest request) {

        ErrorResponse body = buildErrorResponse(
                ErrorCodes.SERVICE_COMMUNICATION_ERROR,
                "Downstream service is unreachable",
                null,
                request.getRequestURI());

        log.error("Downstream service unreachable [traceId={}]: {}",
                body.getTraceId(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClient(
            RestClientException ex,
            HttpServletRequest request) {

        ErrorResponse body = buildErrorResponse(
                ErrorCodes.SERVICE_COMMUNICATION_ERROR,
                "Error communicating with downstream service",
                null,
                request.getRequestURI());

        log.error("REST client error [traceId={}]: {}", body.getTraceId(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    // =========================================================================
    // Catch-all — 500 Internal Server Error
    // =========================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaught(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse body = buildErrorResponse(
                ErrorCodes.INTERNAL_ERROR,
                "An unexpected error occurred",
                null,
                request.getRequestURI());

        log.error("Unhandled exception [traceId={}]: {}", body.getTraceId(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    private ErrorResponse buildErrorResponse(
            String errorCode,
            String message,
            List<ErrorResponse.FieldError> details,
            String path) {

        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .timestamp(Instant.now())
                .traceId(getCurrentTraceId())
                .path(path)
                .build();
    }

    private String getCurrentTraceId() {
        if (tracer == null || tracer.currentSpan() == null || tracer.currentSpan().context() == null) {
            return null;
        }
        String traceId = tracer.currentSpan().context().traceId();
        return (traceId == null || traceId.isEmpty()) ? null : traceId;
    }

    private String getRequestPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    private String extractFieldName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int lastDot = path.lastIndexOf('.');
        return lastDot >= 0 ? path.substring(lastDot + 1) : path;
    }
}
