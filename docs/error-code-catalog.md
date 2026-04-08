# FTGO Error Code Catalog

This document describes the standardized error codes used across all FTGO microservices.
Every error response follows the format defined by `ErrorResponse` in `ftgo-error-handling-lib`.

## Error Response Format

```json
{
  "errorCode": "FTGO_VALIDATION_ERROR",
  "message": "Validation failed",
  "details": [
    {
      "field": "consumerId",
      "message": "Consumer ID is required",
      "rejectedValue": null
    }
  ],
  "timestamp": "2026-04-08T15:30:00Z",
  "traceId": "abc123def456",
  "path": "/api/v1/orders"
}
```

| Field       | Type              | Description                                           |
|-------------|-------------------|-------------------------------------------------------|
| `errorCode` | `String`          | Machine-readable error code (see tables below)        |
| `message`   | `String`          | Human-readable error description                      |
| `details`   | `FieldError[]`    | Field-level validation errors (nullable)              |
| `timestamp` | `ISO-8601 String` | When the error occurred                               |
| `traceId`   | `String`          | Distributed trace ID for debugging (nullable)         |
| `path`      | `String`          | Request URI that triggered the error                  |

---

## Generic Error Codes

These codes apply across all services.

| Error Code                     | HTTP Status | Description                                              |
|--------------------------------|-------------|----------------------------------------------------------|
| `FTGO_VALIDATION_ERROR`        | 400         | Request body failed Bean Validation (field-level details) |
| `FTGO_MALFORMED_REQUEST`       | 400         | Request body is not valid JSON or cannot be deserialized  |
| `FTGO_RESOURCE_NOT_FOUND`      | 404         | Requested entity does not exist                           |
| `FTGO_STATE_CONFLICT`          | 409         | Operation not allowed in the entity's current state       |
| `FTGO_UNPROCESSABLE_ENTITY`    | 422         | Request is well-formed but violates a business rule       |
| `FTGO_UNAUTHORIZED`            | 401         | Missing or invalid authentication credentials             |
| `FTGO_FORBIDDEN`               | 403         | Authenticated but insufficient permissions                |
| `FTGO_INTERNAL_ERROR`          | 500         | Unexpected server error (no internal details exposed)     |
| `FTGO_SERVICE_COMMUNICATION_ERROR` | 502     | Failed to reach a downstream microservice                 |
| `FTGO_METHOD_NOT_ALLOWED`      | 405         | HTTP method not supported for this endpoint               |
| `FTGO_UNSUPPORTED_MEDIA_TYPE`  | 415         | Content-Type header not supported                         |

---

## Domain-Specific Error Codes

### Order Service

| Error Code                  | HTTP Status | Description                                              |
|-----------------------------|-------------|----------------------------------------------------------|
| `ORDER_STATE_CONFLICT`      | 409         | Order cannot transition to the requested state            |
| `ORDER_MINIMUM_NOT_MET`     | 422         | Order total is below the restaurant's minimum             |
| `ORDER_NOT_FOUND`           | 404         | Order with the given ID does not exist                    |

### Consumer Service

| Error Code                  | HTTP Status | Description                                              |
|-----------------------------|-------------|----------------------------------------------------------|
| `CONSUMER_NOT_FOUND`        | 404         | Consumer with the given ID does not exist                 |

### Restaurant Service

| Error Code                  | HTTP Status | Description                                              |
|-----------------------------|-------------|----------------------------------------------------------|
| `RESTAURANT_NOT_FOUND`      | 404         | Restaurant with the given ID does not exist               |

### Courier Service

| Error Code                  | HTTP Status | Description                                              |
|-----------------------------|-------------|----------------------------------------------------------|
| `COURIER_NOT_FOUND`         | 404         | Courier with the given ID does not exist                  |

---

## Validation Error Details

When `errorCode` is `FTGO_VALIDATION_ERROR`, the `details` array contains one entry per
invalid field:

```json
{
  "errorCode": "FTGO_VALIDATION_ERROR",
  "message": "Validation failed",
  "details": [
    {
      "field": "name",
      "message": "must not be blank",
      "rejectedValue": ""
    },
    {
      "field": "quantity",
      "message": "must be greater than or equal to 1",
      "rejectedValue": -1
    }
  ],
  "timestamp": "2026-04-08T15:30:00Z",
  "traceId": "abc123def456",
  "path": "/api/v1/orders"
}
```

---

## Exception-to-HTTP Mapping

| Java Exception                            | HTTP Status | Error Code                        |
|-------------------------------------------|-------------|-----------------------------------|
| `MethodArgumentNotValidException`         | 400         | `FTGO_VALIDATION_ERROR`           |
| `ConstraintViolationException`            | 400         | `FTGO_VALIDATION_ERROR`           |
| `HttpMessageNotReadableException`         | 400         | `FTGO_MALFORMED_REQUEST`          |
| `HttpRequestMethodNotSupportedException`  | 405         | `FTGO_METHOD_NOT_ALLOWED`         |
| `HttpMediaTypeNotSupportedException`      | 415         | `FTGO_UNSUPPORTED_MEDIA_TYPE`     |
| `ResourceNotFoundException`               | 404         | `FTGO_RESOURCE_NOT_FOUND` (or domain-specific) |
| `UnsupportedStateTransitionException`     | 409         | `FTGO_STATE_CONFLICT`             |
| `OrderMinimumNotMetException`             | 422         | `ORDER_MINIMUM_NOT_MET`           |
| `NotYetImplementedException`              | 501         | `FTGO_INTERNAL_ERROR`             |
| `ServiceCommunicationException`           | 502         | `FTGO_SERVICE_COMMUNICATION_ERROR`|
| `ResourceAccessException`                 | 502         | `FTGO_SERVICE_COMMUNICATION_ERROR`|
| `RestClientException`                     | 502         | `FTGO_SERVICE_COMMUNICATION_ERROR`|
| `Exception` (catch-all)                   | 500         | `FTGO_INTERNAL_ERROR`             |

---

## Usage

### Adding the library to a service

```groovy
// In your service's build.gradle
dependencies {
    implementation project(":shared:ftgo-error-handling-lib")
}
```

The `GlobalExceptionHandler` is auto-configured via Spring Boot's auto-configuration
mechanism. No additional configuration is required.

### Throwing domain exceptions

```java
// 404 — Resource not found
throw new ResourceNotFoundException("Order", orderId, ErrorCodes.ORDER_NOT_FOUND);

// 502 — Service communication failure
throw new ServiceCommunicationException("consumer-service", "Connection refused", cause);
```

### Bean Validation on request DTOs

```java
@PostMapping
public ResponseEntity<Void> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    // Validation errors automatically return 400 with field-level details
}
```
