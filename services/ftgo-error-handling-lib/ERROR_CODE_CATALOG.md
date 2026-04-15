# FTGO Error Code Catalog

All FTGO microservices return errors in a standardized format. This document catalogs every error code, its HTTP status, and when it is returned.

## Error Response Format

```json
{
  "code": "FTGO-400-001",
  "message": "Request validation failed",
  "details": [
    {
      "field": "name",
      "message": "must not be blank",
      "rejectedValue": ""
    }
  ],
  "timestamp": "2024-01-15T10:30:00Z",
  "traceId": "64f8a1b2c3d4e5f6"
}
```

| Field       | Type            | Always Present | Description                                              |
|-------------|-----------------|----------------|----------------------------------------------------------|
| `code`      | `String`        | Yes            | Machine-readable error code (`FTGO-NNN-NNN`)             |
| `message`   | `String`        | Yes            | Human-readable error description                         |
| `details`   | `FieldError[]`  | No             | Field-level validation errors (only for 400 validation)  |
| `timestamp` | `Instant`       | Yes            | ISO-8601 instant when the error occurred                 |
| `traceId`   | `String`        | No             | Distributed tracing ID for correlation (when available)  |

## Error Codes

### Validation Errors (400 Bad Request)

| Code           | Name                       | Description                                         |
|----------------|----------------------------|-----------------------------------------------------|
| `FTGO-400-001` | `VALIDATION_FAILED`        | Bean Validation failed. `details` contains per-field errors. |
| `FTGO-400-002` | `INVALID_REQUEST_BODY`     | Request body is malformed or unreadable JSON.       |
| `FTGO-400-003` | `MISSING_REQUIRED_FIELD`   | A required field is missing from the request.       |
| `FTGO-400-004` | `TYPE_MISMATCH`            | A path/query parameter has an invalid type.         |
| `FTGO-400-005` | `MISSING_REQUEST_PARAMETER`| A required query/path parameter is missing.         |

### Authentication Errors (401 Unauthorized)

| Code           | Name                       | Description                                         |
|----------------|----------------------------|-----------------------------------------------------|
| `FTGO-401-001` | `AUTHENTICATION_REQUIRED`  | No valid credentials were provided.                 |

### Authorization Errors (403 Forbidden)

| Code           | Name                       | Description                                         |
|----------------|----------------------------|-----------------------------------------------------|
| `FTGO-403-001` | `ACCESS_DENIED`            | The authenticated user lacks permission.            |

### Not Found Errors (404 Not Found)

| Code           | Name                       | Description                                         |
|----------------|----------------------------|-----------------------------------------------------|
| `FTGO-404-001` | `RESOURCE_NOT_FOUND`       | Generic resource not found.                         |
| `FTGO-404-002` | `ORDER_NOT_FOUND`          | The specified order does not exist.                 |
| `FTGO-404-003` | `CONSUMER_NOT_FOUND`       | The specified consumer does not exist.              |
| `FTGO-404-004` | `RESTAURANT_NOT_FOUND`     | The specified restaurant does not exist.            |
| `FTGO-404-005` | `COURIER_NOT_FOUND`        | The specified courier does not exist.               |

### Method Not Allowed (405)

| Code           | Name                       | Description                                         |
|----------------|----------------------------|-----------------------------------------------------|
| `FTGO-405-001` | `METHOD_NOT_ALLOWED`       | The HTTP method is not supported for this endpoint. |

### Conflict Errors (409 Conflict)

| Code           | Name                         | Description                                       |
|----------------|------------------------------|---------------------------------------------------|
| `FTGO-409-001` | `STATE_TRANSITION_CONFLICT`  | Invalid state transition (e.g. cancelling a delivered order). Maps from `UnsupportedStateTransitionException`. |
| `FTGO-409-002` | `RESOURCE_CONFLICT`          | Generic conflict with current resource state.     |

### Unprocessable Entity (422)

| Code           | Name                       | Description                                         |
|----------------|----------------------------|-----------------------------------------------------|
| `FTGO-422-001` | `ORDER_MINIMUM_NOT_MET`    | Order total is below the restaurant minimum. Maps from `OrderMinimumNotMetException`. |
| `FTGO-422-002` | `BUSINESS_RULE_VIOLATION`  | A domain business rule prevented the operation.     |

### Internal Server Error (500)

| Code           | Name                       | Description                                         |
|----------------|----------------------------|-----------------------------------------------------|
| `FTGO-500-001` | `INTERNAL_ERROR`           | Unexpected internal error. No details are leaked.   |

### Service Communication Errors (502/503)

| Code           | Name                              | Description                                   |
|----------------|-----------------------------------|-----------------------------------------------|
| `FTGO-502-001` | `SERVICE_COMMUNICATION_FAILURE`   | Failed to reach a downstream service.         |
| `FTGO-503-001` | `SERVICE_UNAVAILABLE`             | A required service is temporarily unavailable.|

## Exception-to-HTTP Mapping

| Exception Class                        | HTTP Status | Error Code        |
|----------------------------------------|-------------|-------------------|
| `MethodArgumentNotValidException`      | 400         | `FTGO-400-001`    |
| `HttpMessageNotReadableException`      | 400         | `FTGO-400-002`    |
| `MissingServletRequestParameterException` | 400      | `FTGO-400-005`    |
| `MethodArgumentTypeMismatchException`  | 400         | `FTGO-400-004`    |
| `ResourceNotFoundException`            | 404         | varies by domain  |
| `HttpRequestMethodNotSupportedException`| 405        | `FTGO-405-001`    |
| `UnsupportedStateTransitionException`  | 409         | `FTGO-409-001`    |
| `OrderMinimumNotMetException`          | 422         | `FTGO-422-001`    |
| `BusinessRuleException`               | 422         | varies            |
| `ServiceCommunicationException`        | 502         | `FTGO-502-001`    |
| `Exception` (catch-all)               | 500         | `FTGO-500-001`    |

## Usage

### Throwing domain exceptions

```java
// Not found
throw new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, "Order " + id + " not found");

// Business rule violation
throw new BusinessRuleException(ErrorCode.ORDER_MINIMUM_NOT_MET, "Order total $5.00 is below minimum $10.00");

// Inter-service communication failure
throw new ServiceCommunicationException("restaurant-service", "Connection refused", cause);
```

### Adding Bean Validation to DTOs

```java
public class CreateOrderRequest {
    @Positive(message = "restaurantId must be positive")
    private long restaurantId;

    @NotEmpty(message = "lineItems must not be empty")
    @Valid
    private List<LineItem> lineItems;
}
```

Controllers must use `@Valid` on `@RequestBody` parameters:

```java
@PostMapping
public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    // ...
}
```
