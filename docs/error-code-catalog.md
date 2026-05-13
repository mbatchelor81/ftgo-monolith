# FTGO Error Code Catalog

Standardized error codes used across all FTGO platform microservices.
Implemented by `ftgo-error-handling-lib` (`libs/ftgo-error-handling-lib/`).

## Error Response Format

Every error response follows this JSON structure:

```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "Order not found with id 42",
  "details": null,
  "timestamp": "2024-06-15T10:30:00.000Z",
  "traceId": "64a8c3e1f0b2d4a6"
}
```

| Field       | Type                | Description                                               |
|-------------|---------------------|-----------------------------------------------------------|
| `code`      | `string`            | Machine-readable error code from the catalog below        |
| `message`   | `string`            | Human-readable description of the error                   |
| `details`   | `array` or `null`   | Field-level validation errors (only for `VALIDATION_ERROR`) |
| `timestamp` | `string` (ISO 8601) | Time the error occurred                                   |
| `traceId`   | `string` or `null`  | Distributed trace ID for request correlation              |

### Validation Error Detail

When `code` is `VALIDATION_ERROR`, the `details` array contains:

```json
{
  "field": "quantity",
  "rejectedValue": -1,
  "message": "must be greater than 0"
}
```

---

## Error Code Reference

### Client Errors (4xx)

| Code                         | HTTP Status | Default Message                                          | When Used                                                           |
|------------------------------|-------------|----------------------------------------------------------|---------------------------------------------------------------------|
| `VALIDATION_ERROR`           | 400         | One or more fields failed validation                     | Bean Validation (`@Valid`) constraint violations                    |
| `INVALID_REQUEST`            | 400         | The request body is malformed or unreadable              | Malformed JSON, missing required parameters, type mismatches        |
| `RESOURCE_NOT_FOUND`         | 404         | The requested resource does not exist                    | Entity lookups that return no result (e.g., `OrderNotFoundException`) |
| `METHOD_NOT_ALLOWED`         | 405         | The HTTP method is not supported for this endpoint       | Using an unsupported HTTP method on an endpoint                     |
| `STATE_CONFLICT`             | 409         | The operation conflicts with the current resource state  | `UnsupportedStateTransitionException`, `IllegalStateException`      |
| `OPTIMISTIC_LOCK_CONFLICT`   | 409         | The resource was modified by another request             | `OptimisticOfflineLockException` (concurrent modification)          |
| `UNSUPPORTED_MEDIA_TYPE`     | 415         | The request content type is not supported                | Sending a non-JSON content type to a JSON endpoint                  |
| `BUSINESS_RULE_VIOLATION`    | 422         | A business rule prevented the operation                  | Generic business rule failure                                       |
| `ORDER_MINIMUM_NOT_MET`      | 422         | The order total does not meet the restaurant minimum     | `OrderMinimumNotMetException`                                       |
| `CONSUMER_VERIFICATION_FAILED` | 422       | Consumer verification failed                             | `ConsumerVerificationFailedException`                               |

### Server Errors (5xx)

| Code                  | HTTP Status | Default Message                                    | When Used                                                     |
|-----------------------|-------------|-----------------------------------------------------|---------------------------------------------------------------|
| `INTERNAL_ERROR`      | 500         | An unexpected internal error occurred               | Unhandled exceptions (catch-all)                              |
| `NOT_IMPLEMENTED`     | 501         | This operation is not yet implemented               | `NotYetImplementedException`, `UnsupportedOperationException` |
| `SERVICE_UNAVAILABLE` | 503         | A downstream service is temporarily unavailable     | `ConnectException` during inter-service communication         |

---

## Exception-to-Error-Code Mapping

The `GlobalExceptionHandler` maps exceptions to error codes using the following rules:

### By Exception Type (compile-time)

| Exception                                  | Error Code              |
|--------------------------------------------|-------------------------|
| `MethodArgumentNotValidException`          | `VALIDATION_ERROR`      |
| `HttpMessageNotReadableException`          | `INVALID_REQUEST`       |
| `MissingServletRequestParameterException`  | `INVALID_REQUEST`       |
| `MethodArgumentTypeMismatchException`      | `INVALID_REQUEST`       |
| `IllegalArgumentException`                 | `INVALID_REQUEST`       |
| `IllegalStateException`                    | `STATE_CONFLICT`        |
| `HttpRequestMethodNotSupportedException`   | `METHOD_NOT_ALLOWED`    |
| `HttpMediaTypeNotSupportedException`       | `UNSUPPORTED_MEDIA_TYPE`|
| `UnsupportedOperationException`            | `NOT_IMPLEMENTED`       |
| `ConnectException`                         | `SERVICE_UNAVAILABLE`   |
| `NoHandlerFoundException`                  | `RESOURCE_NOT_FOUND`    |

### By Class Name Convention (runtime, avoids coupling)

| Class Name Pattern                         | Error Code                    |
|--------------------------------------------|-------------------------------|
| `*NotFoundException`                       | `RESOURCE_NOT_FOUND`          |
| `UnsupportedStateTransitionException`      | `STATE_CONFLICT`              |
| `OptimisticOfflineLockException`           | `OPTIMISTIC_LOCK_CONFLICT`    |
| `OrderMinimumNotMetException`              | `ORDER_MINIMUM_NOT_MET`       |
| `InvalidMenuItemIdException`              | `INVALID_REQUEST`             |
| `ConsumerVerificationFailedException`      | `CONSUMER_VERIFICATION_FAILED`|
| `NotYetImplementedException`               | `NOT_IMPLEMENTED`             |

Any `RuntimeException` not matching the above patterns returns `INTERNAL_ERROR` (500).

---

## Usage

### Adding to a Microservice

Add `ftgo-error-handling-lib` as a dependency:

```gradle
dependencies {
    implementation project(':ftgo-error-handling-lib')
    implementation project(':ftgo-tracing-lib')
}
```

The `GlobalExceptionHandler` is auto-configured via Spring Boot's
`AutoConfiguration` mechanism. No additional setup is required beyond
having a `Tracer` bean in the application context (provided by `ftgo-tracing-lib`).

### Disabling

Set `ftgo.error-handling.enabled=false` in `application.properties` to disable
the auto-configured handler.

### Custom Exception Handling

Services can define additional `@ExceptionHandler` methods in their own
`@RestControllerAdvice` classes. Spring will prefer more specific handlers.

---

## Trace ID Integration

Every error response includes a `traceId` field populated from Micrometer Tracing
(provided by `ftgo-tracing-lib`, EM-42). This enables:

- Correlating error responses with distributed traces in Zipkin
- Searching for the full request flow when a user reports an error
- Linking error logs to traces in observability dashboards
