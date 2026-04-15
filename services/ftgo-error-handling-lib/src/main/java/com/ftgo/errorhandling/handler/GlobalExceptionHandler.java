package com.ftgo.errorhandling.handler;

import com.ftgo.common.UnsupportedStateTransitionException;
import com.ftgo.domain.OrderMinimumNotMetException;
import com.ftgo.errorhandling.exception.BusinessRuleException;
import com.ftgo.errorhandling.exception.ResourceNotFoundException;
import com.ftgo.errorhandling.exception.ServiceCommunicationException;
import com.ftgo.errorhandling.model.ErrorCode;
import com.ftgo.errorhandling.model.ErrorResponse;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Centralized exception handler for all FTGO microservices.
 *
 * <p>Converts exceptions into a standardized {@link ErrorResponse} containing: error code, message,
 * field-level details (for validation errors), timestamp, and distributed traceId. No stack traces
 * are ever leaked to clients.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    // ---- Validation errors (400) ----

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<ErrorResponse.FieldError> fieldErrors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(
                                fe ->
                                        new ErrorResponse.FieldError(
                                                fe.getField(),
                                                fe.getDefaultMessage(),
                                                fe.getRejectedValue()))
                        .collect(Collectors.toList());

        ErrorResponse response = buildResponse(ErrorCode.VALIDATION_FAILED);
        response.setDetails(fieldErrors);

        LOG.warn("Validation failed: {} field error(s)", fieldErrors.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ErrorResponse response = buildResponse(ErrorCode.INVALID_REQUEST_BODY);
        LOG.warn("Malformed request body: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ErrorResponse response =
                buildResponse(
                        ErrorCode.MISSING_REQUEST_PARAMETER,
                        "Required parameter '"
                                + ex.getParameterName()
                                + "' of type "
                                + ex.getParameterType()
                                + " is missing");

        LOG.warn("Missing request parameter: {}", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        Class<?> reqType = ex.getRequiredType();
        String requiredType = reqType != null ? reqType.getSimpleName() : "unknown";
        ErrorResponse response =
                buildResponse(
                        ErrorCode.TYPE_MISMATCH,
                        "Parameter '" + ex.getName() + "' should be of type " + requiredType);

        LOG.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ---- Not Found (404) ----

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse response = buildResponse(ex.getErrorCode(), ex.getMessage());
        LOG.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ---- Method Not Allowed (405) ----

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ErrorResponse response =
                buildResponse(
                        ErrorCode.METHOD_NOT_ALLOWED,
                        "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint");

        LOG.warn("Method not allowed: {}", ex.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // ---- Conflict (409) ----

    @ExceptionHandler(UnsupportedStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleStateTransitionConflict(
            UnsupportedStateTransitionException ex) {

        ErrorResponse response =
                buildResponse(ErrorCode.STATE_TRANSITION_CONFLICT, ex.getMessage());
        LOG.warn("Invalid state transition: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // ---- Unprocessable Entity (422) ----

    @ExceptionHandler(OrderMinimumNotMetException.class)
    public ResponseEntity<ErrorResponse> handleOrderMinimumNotMet(OrderMinimumNotMetException ex) {

        ErrorResponse response =
                buildResponse(
                        ErrorCode.ORDER_MINIMUM_NOT_MET,
                        ErrorCode.ORDER_MINIMUM_NOT_MET.getDefaultMessage());
        LOG.warn("Order minimum not met");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
        ErrorResponse response = buildResponse(ex.getErrorCode(), ex.getMessage());
        LOG.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    // ---- Service Communication (502) ----

    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<ErrorResponse> handleServiceCommunication(
            ServiceCommunicationException ex) {

        ErrorResponse response = buildResponse(ex.getErrorCode(), ex.getMessage());
        LOG.error(
                "Service communication failure [service={}]: {}",
                ex.getServiceName(),
                ex.getMessage(),
                ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    // ---- Security exceptions (401 / 403) ----

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        ErrorResponse response = buildResponse(ErrorCode.AUTHENTICATION_REQUIRED);
        LOG.warn("Authentication required: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse response = buildResponse(ErrorCode.ACCESS_DENIED);
        LOG.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ---- Catch-all (500) ----

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUncaught(Exception ex) {
        ErrorResponse response = buildResponse(ErrorCode.INTERNAL_ERROR);
        LOG.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ---- Helper methods ----

    private ErrorResponse buildResponse(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(), errorCode.getDefaultMessage(), currentTraceId());
    }

    private ErrorResponse buildResponse(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getCode(), message, currentTraceId());
    }

    private String currentTraceId() {
        if (tracer == null) {
            return null;
        }
        Span span = tracer.currentSpan();
        if (span == null) {
            return null;
        }
        TraceContext context = span.context();
        if (context == null) {
            return null;
        }
        return context.traceId();
    }
}
