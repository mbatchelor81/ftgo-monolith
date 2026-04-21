package net.chrisrichardson.ftgo.common.errors;

import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * Centralized {@link org.springframework.web.bind.annotation.RestControllerAdvice}
 * that turns every uncaught exception into a consistent {@link ErrorResponse}.
 *
 * <p>Each FTGO microservice imports this class (directly or via a thin
 * service-specific subclass) so clients see the same error contract regardless
 * of which service they call. See {@code docs/error-code-catalog.md} for the
 * complete list of codes emitted here.
 *
 * <p>No stack traces are ever sent to clients — {@link #handleGeneric} scrubs
 * the exception and logs it at ERROR while returning only a generic
 * {@link ErrorCode#INTERNAL_ERROR} payload.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // --- Domain exceptions (FtgoException hierarchy) ------------------------

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      EntityNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getMessage(), request);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflict(
      ConflictException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getMessage(), request);
  }

  @ExceptionHandler(BusinessRuleViolationException.class)
  public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(
      BusinessRuleViolationException ex, HttpServletRequest request) {
    return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getErrorCode(), ex.getMessage(), request);
  }

  @ExceptionHandler(ServiceUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleServiceUnavailable(
      ServiceUnavailableException ex, HttpServletRequest request) {
    logger.warn("Downstream service unavailable: {}", ex.getMessage(), ex);
    return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getErrorCode(), ex.getMessage(), request);
  }

  // --- Inter-service communication failures ------------------------------

  /**
   * Downstream service returned a non-2xx response. We propagate the
   * downstream status only when it is itself a 5xx (gateway-style) and
   * otherwise collapse to 502 Bad Gateway — clients should never be able to
   * probe a downstream's 404s through our API.
   */
  @ExceptionHandler(HttpStatusCodeException.class)
  public ResponseEntity<ErrorResponse> handleDownstreamHttpError(
      HttpStatusCodeException ex, HttpServletRequest request) {
    logger.warn("Downstream HTTP error {}: {}", ex.getStatusCode(), ex.getMessage());
    return build(HttpStatus.BAD_GATEWAY, ErrorCode.SERVICE_UNAVAILABLE,
        "Downstream service returned an error", request);
  }

  /**
   * Connection-level failures from {@code RestTemplate} / {@code WebClient}:
   * I/O exceptions, DNS failures, timeouts. Treated as 503 because the
   * downstream is effectively unreachable.
   */
  @ExceptionHandler(ResourceAccessException.class)
  public ResponseEntity<ErrorResponse> handleDownstreamTimeout(
      ResourceAccessException ex, HttpServletRequest request) {
    logger.warn("Downstream service unreachable: {}", ex.getMessage(), ex);
    return build(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.SERVICE_UNAVAILABLE,
        "A downstream dependency did not respond in time", request);
  }

  /**
   * Catch-all for any other {@link RestClientException} subclass we didn't
   * explicitly handle above — e.g. {@code UnknownContentTypeException}.
   * Treated as 502 since we did reach the downstream but couldn't make
   * sense of the reply.
   */
  @ExceptionHandler(RestClientException.class)
  public ResponseEntity<ErrorResponse> handleRestClientFailure(
      RestClientException ex, HttpServletRequest request) {
    logger.warn("Downstream REST client failure: {}", ex.getMessage(), ex);
    return build(HttpStatus.BAD_GATEWAY, ErrorCode.SERVICE_UNAVAILABLE,
        "Downstream service call failed", request);
  }

  @ExceptionHandler(FtgoException.class)
  public ResponseEntity<ErrorResponse> handleFtgoException(
      FtgoException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage(), request);
  }

  // --- Legacy domain exceptions still in the codebase ---------------------

  /**
   * Legacy exception thrown by {@code Order.cancel()}, {@code Order.accept()},
   * etc. when the current state forbids the requested transition. Mapped to
   * HTTP 409 Conflict per the migration requirements.
   */
  @ExceptionHandler(UnsupportedStateTransitionException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedStateTransition(
      UnsupportedStateTransitionException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ErrorCode.ORDER_STATE_INVALID,
        "Requested action is not allowed in the current state", request);
  }

  // --- Bean Validation ----------------------------------------------------

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    ErrorResponse body = buildBody(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED,
        ErrorCode.VALIDATION_FAILED.defaultMessage(), request);
    ex.getBindingResult().getFieldErrors().forEach(fe ->
        body.addFieldError(fe.getField(), fe.getDefaultMessage()));
    ex.getBindingResult().getGlobalErrors().forEach(ge ->
        body.addFieldError(ge.getObjectName(), ge.getDefaultMessage()));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    ErrorResponse body = buildBody(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED,
        ErrorCode.VALIDATION_FAILED.defaultMessage(), request);
    for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
      body.addFieldError(v.getPropertyPath().toString(), v.getMessage());
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  // --- Spring MVC request-mapping errors ----------------------------------

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleMessageNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, ErrorCode.MESSAGE_NOT_READABLE,
        ErrorCode.MESSAGE_NOT_READABLE.defaultMessage(), request);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParameter(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_PARAMETER,
        "Missing required parameter: " + ex.getParameterName(), request);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, ErrorCode.TYPE_MISMATCH,
        "Parameter '" + ex.getName() + "' has the wrong type", request);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    return build(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED,
        ex.getMessage(), request);
  }

  // --- Fallback -----------------------------------------------------------

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED,
        ex.getMessage() == null ? ErrorCode.VALIDATION_FAILED.defaultMessage() : ex.getMessage(),
        request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
    logger.error("Unhandled exception at {}: {}",
        request == null ? "(unknown)" : request.getRequestURI(), ex.getMessage(), ex);
    // Deliberately do NOT surface ex.getMessage() — it may contain stack-trace
    // noise, DB schema hints, or internal IDs that must not leak to clients.
    return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR,
        ErrorCode.INTERNAL_ERROR.defaultMessage(), request);
  }

  // --- Helpers ------------------------------------------------------------

  private ResponseEntity<ErrorResponse> build(HttpStatus status, ErrorCode code, String message,
                                              HttpServletRequest request) {
    return ResponseEntity.status(status).body(buildBody(status, code, message, request));
  }

  private ErrorResponse buildBody(HttpStatus status, ErrorCode code, String message,
                                  HttpServletRequest request) {
    ErrorResponse body = new ErrorResponse(code.code(),
        message == null ? code.defaultMessage() : message, status.value());
    if (request != null) {
      body.setPath(request.getRequestURI());
    }
    body.setTraceId(currentTraceId());
    return body;
  }

  /**
   * Best-effort trace-id lookup for correlation with Zipkin / log aggregators.
   * Reads the {@code traceId} MDC key populated by Micrometer Tracing (EM-42),
   * falling back to the Brave-compatible {@code X-B3-TraceId} key for
   * environments still on the Sleuth contract.
   */
  private static String currentTraceId() {
    String traceId = MDC.get("traceId");
    if (traceId == null) {
      traceId = MDC.get("X-B3-TraceId");
    }
    return traceId;
  }
}
