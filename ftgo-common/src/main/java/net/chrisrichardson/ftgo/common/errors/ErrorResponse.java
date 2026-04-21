package net.chrisrichardson.ftgo.common.errors;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Standard error payload returned by every FTGO REST endpoint.
 *
 * <p>The shape is frozen: clients may rely on the {@code errorCode} field to
 * drive recovery logic and on {@code traceId} for correlation with
 * observability tools. The {@code fieldErrors} list is populated for
 * validation failures and is omitted from the JSON payload when empty.
 *
 * <p>See {@code docs/error-code-catalog.md} for the full list of codes.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

  private String errorCode;
  private String message;
  private int status;
  private String path;
  private Instant timestamp;
  private String traceId;
  private String details;
  private List<FieldError> fieldErrors;

  public ErrorResponse() {
    this.timestamp = Instant.now();
    this.fieldErrors = Collections.emptyList();
  }

  public ErrorResponse(String errorCode, String message, int status) {
    this();
    this.errorCode = errorCode;
    this.message = message;
    this.status = status;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public String getTraceId() {
    return traceId;
  }

  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public List<FieldError> getFieldErrors() {
    return fieldErrors;
  }

  public void setFieldErrors(List<FieldError> fieldErrors) {
    this.fieldErrors = fieldErrors == null ? Collections.emptyList() : fieldErrors;
  }

  public ErrorResponse addFieldError(String field, String message) {
    if (this.fieldErrors.isEmpty()) {
      this.fieldErrors = new ArrayList<>();
    }
    this.fieldErrors.add(new FieldError(field, message));
    return this;
  }

  /**
   * A single field-level validation error. Populated for HTTP 400 responses
   * triggered by Bean Validation on request DTOs.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class FieldError {
    private String field;
    private String message;

    public FieldError() {
    }

    public FieldError(String field, String message) {
      this.field = field;
      this.message = message;
    }

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }
}
