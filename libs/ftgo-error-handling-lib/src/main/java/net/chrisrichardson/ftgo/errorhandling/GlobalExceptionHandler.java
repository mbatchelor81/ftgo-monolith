package net.chrisrichardson.ftgo.errorhandling;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.ConnectException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for all FTGO platform microservices.
 * Translates exceptions into standardized {@link ErrorResponse} payloads.
 *
 * <p>Integrates with Micrometer Tracing to include the current traceId
 * in every error response, enabling end-to-end request correlation.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    // --- Bean Validation errors ---

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        List<ValidationErrorDetail> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ValidationErrorDetail(
                        fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorResponse body = ErrorResponse.withValidationErrors(
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(), details, currentTraceId());

        log.warn("Validation failed: {} field error(s)", details.size());
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getHttpStatus()).body(body);
    }

    // --- Malformed request body ---

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.INVALID_REQUEST,
                "Request body is missing or malformed", currentTraceId());
        log.warn("Message not readable: {}", ex.getMessage());
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getHttpStatus()).body(body);
    }

    // --- HTTP method not supported ---

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED,
                "Method '" + ex.getMethod() + "' is not supported for this endpoint",
                currentTraceId());
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus()).body(body);
    }

    // --- Unsupported media type ---

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                currentTraceId());
        return ResponseEntity.status(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getHttpStatus()).body(body);
    }

    // --- Missing request parameter ---

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.INVALID_REQUEST,
                "Required parameter '" + ex.getParameterName() + "' is missing",
                currentTraceId());
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getHttpStatus()).body(body);
    }

    // --- No handler / resource found ---

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND,
                "No endpoint found for " + ex.getHttpMethod() + " " + ex.getRequestURL(),
                currentTraceId());
        return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus()).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND,
                ex.getMessage(), currentTraceId());
        return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus()).body(body);
    }

    // --- FTGO Domain Exceptions (matched by class name to avoid coupling) ---

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.INVALID_REQUEST, ex.getMessage(),
                currentTraceId());
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getHttpStatus()).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.STATE_CONFLICT, ex.getMessage(),
                currentTraceId());
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(ErrorCode.STATE_CONFLICT.getHttpStatus()).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String message = "Parameter '" + ex.getName() + "' must be of type "
                + (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        ErrorResponse body = ErrorResponse.of(ErrorCode.INVALID_REQUEST, message,
                currentTraceId());
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getHttpStatus()).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUnsupportedOperationException(
            UnsupportedOperationException ex) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.NOT_IMPLEMENTED, ex.getMessage(),
                currentTraceId());
        return ResponseEntity.status(ErrorCode.NOT_IMPLEMENTED.getHttpStatus()).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleConnectException(ConnectException ex) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.SERVICE_UNAVAILABLE,
                "Failed to connect to downstream service", currentTraceId());
        log.error("Downstream service connection failure", ex);
        return ResponseEntity.status(ErrorCode.SERVICE_UNAVAILABLE.getHttpStatus()).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorCode errorCode = resolveErrorCode(ex);
        String message = (errorCode != ErrorCode.INTERNAL_ERROR && ex.getMessage() != null)
                ? ex.getMessage() : errorCode.getDefaultMessage();

        if (errorCode == ErrorCode.INTERNAL_ERROR) {
            log.error("Unhandled exception", ex);
        } else {
            log.warn("{}: {}", errorCode, message);
        }

        ErrorResponse body = ErrorResponse.of(errorCode, message, currentTraceId());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled checked exception", ex);
        ErrorResponse body = ErrorResponse.of(ErrorCode.INTERNAL_ERROR, currentTraceId());
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getHttpStatus()).body(body);
    }

    /**
     * Resolves the appropriate {@link ErrorCode} for a RuntimeException based on
     * its class name. This avoids compile-time coupling to domain modules while
     * still mapping FTGO exceptions to correct HTTP semantics.
     */
    private ErrorCode resolveErrorCode(RuntimeException ex) {
        String className = ex.getClass().getSimpleName();

        // Exact name matches first (higher priority than suffix patterns)
        if (className.equals("UnsupportedStateTransitionException")) {
            return ErrorCode.STATE_CONFLICT;
        }
        if (className.equals("OptimisticOfflineLockException")) {
            return ErrorCode.OPTIMISTIC_LOCK_CONFLICT;
        }
        if (className.equals("OrderMinimumNotMetException")) {
            return ErrorCode.ORDER_MINIMUM_NOT_MET;
        }
        if (className.equals("InvalidMenuItemIdException")) {
            return ErrorCode.INVALID_REQUEST;
        }
        if (className.equals("ConsumerNotFoundException")
                || className.equals("ConsumerVerificationFailedException")) {
            return ErrorCode.CONSUMER_VERIFICATION_FAILED;
        }
        if (className.equals("NotYetImplementedException")) {
            return ErrorCode.NOT_IMPLEMENTED;
        }

        // Suffix patterns last (catch-all for convention-based naming)
        if (className.endsWith("NotFoundException")) {
            return ErrorCode.RESOURCE_NOT_FOUND;
        }

        return ErrorCode.INTERNAL_ERROR;
    }

    private String currentTraceId() {
        if (tracer == null) {
            return null;
        }
        Span currentSpan = tracer.currentSpan();
        if (currentSpan == null) {
            return null;
        }
        return currentSpan.context().traceId();
    }
}
