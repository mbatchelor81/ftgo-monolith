package com.ftgo.common.error;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/**
 * Global exception handler for all FTGO microservices.
 *
 * <p>Converts exceptions into standardized {@link ErrorResponse} objects,
 * ensuring consistent error format across all services. Includes traceId
 * from MDC when available for distributed tracing correlation.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TRACE_ID_KEY = "traceId";

    /**
     * Handles FTGO domain-specific exceptions.
     */
    @ExceptionHandler(FtgoServiceException.class)
    public ResponseEntity<ErrorResponse> handleFtgoServiceException(
            FtgoServiceException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("Service exception: {} - {}", errorCode.getCode(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(errorCode)
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * Handles Bean Validation errors from @Valid annotations.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(
                        fe.getField(),
                        fe.getDefaultMessage(),
                        fe.getRejectedValue()))
                .toList();

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ErrorCode.VALIDATION_ERROR)
                .message("Validation failed for " + fieldErrors.size() + " field(s)")
                .fieldErrors(fieldErrors)
                .traceId(getTraceId())
                .build();

        log.warn("Validation error: {} field(s) failed", fieldErrors.size());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles malformed request body.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ErrorCode.BAD_REQUEST)
                .message("Malformed request body")
                .traceId(getTraceId())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles unsupported HTTP method.
     */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ErrorCode.METHOD_NOT_ALLOWED)
                .message("Method '" + ex.getMethod() + "' not supported")
                .traceId(getTraceId())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * Handles unsupported media type.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ErrorCode.UNSUPPORTED_MEDIA_TYPE)
                .message("Content type '" + ex.getContentType() + "' not supported")
                .traceId(getTraceId())
                .build();

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    /**
     * Handles missing request parameters.
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ErrorCode.BAD_REQUEST)
                .message("Required parameter '" + ex.getParameterName() + "' is missing")
                .traceId(getTraceId())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles no handler found (404).
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ErrorCode.RESOURCE_NOT_FOUND)
                .message("No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL())
                .traceId(getTraceId())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles type mismatch for method arguments.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format("Parameter '%s' should be of type '%s'",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ErrorCode.BAD_REQUEST)
                .message(message)
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles IllegalArgumentException as bad request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ErrorCode.BAD_REQUEST)
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles IllegalStateException as conflict/unprocessable.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ErrorCode.INVALID_STATE_TRANSITION)
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    /**
     * Catch-all handler for unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtExceptions(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ErrorCode.INTERNAL_ERROR)
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
}
