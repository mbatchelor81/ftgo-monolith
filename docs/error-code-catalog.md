# FTGO Error Code Catalog

Every REST endpoint across every FTGO service returns the same JSON shape
when an error occurs. This document is the authoritative reference for the
error contract — the stable [`ErrorCode`](../ftgo-common/src/main/java/net/chrisrichardson/ftgo/common/errors/ErrorCode.java)
values, the HTTP status each one maps to, and which exception produces it.

Clients **SHOULD** drive recovery logic off the `errorCode` string rather
than the free-form `message` field, since only `errorCode` is part of the
public contract.

## Response Shape

```json
{
  "errorCode":  "FTGO-ORD-001",
  "message":    "Order not found",
  "status":     404,
  "path":       "/orders/42",
  "timestamp":  "2026-04-21T18:40:00Z",
  "traceId":    "b7f1…c3",
  "details":    null,
  "fieldErrors": [
    { "field": "deliveryAddress.street1", "message": "must not be blank" }
  ]
}
```

| Field         | Type     | Present when                                                 |
| ------------- | -------- | ------------------------------------------------------------ |
| `errorCode`   | string   | always                                                       |
| `message`     | string   | always                                                       |
| `status`      | int      | always; mirrors the HTTP status                              |
| `path`        | string   | always (the request URI)                                     |
| `timestamp`   | ISO-8601 | always (UTC instant the response was built)                  |
| `traceId`     | string   | when distributed tracing (EM-42) populated the MDC           |
| `details`     | string   | optional — used for handler-specific elaboration             |
| `fieldErrors` | array    | only on `FTGO-GEN-001` validation failures (else omitted)    |

No error response ever contains a stack trace or raw exception message for
internal errors — [`GlobalExceptionHandler.handleGeneric`](../ftgo-common/src/main/java/net/chrisrichardson/ftgo/common/errors/GlobalExceptionHandler.java)
always substitutes the canonical `FTGO-GEN-999` message.

## Cross-Cutting Codes (`FTGO-GEN-*`)

| Code             | HTTP | Trigger                                                                                   |
| ---------------- | ---: | ----------------------------------------------------------------------------------------- |
| `FTGO-GEN-001`   |  400 | Bean-Validation failure (`MethodArgumentNotValidException`, `ConstraintViolationException`) |
| `FTGO-GEN-002`   |  400 | Malformed / unparseable request body (`HttpMessageNotReadableException`)                  |
| `FTGO-GEN-003`   |  400 | Required query/path parameter missing (`MissingServletRequestParameterException`)         |
| `FTGO-GEN-004`   |  400 | Parameter has wrong type (`MethodArgumentTypeMismatchException`)                          |
| `FTGO-GEN-005`   |  405 | HTTP method not supported on this URI (`HttpRequestMethodNotSupportedException`)          |
| `FTGO-GEN-006`   |  401 | Authentication required (reserved for security module)                                    |
| `FTGO-GEN-007`   |  403 | Caller not permitted (reserved for security module)                                       |
| `FTGO-GEN-008`   |  409 | Generic conflict — request conflicts with current state (base `ConflictException`)        |
| `FTGO-GEN-009`   |  502 / 503 | Inter-service call failed — downstream HTTP error, timeout, or I/O failure          |
| `FTGO-GEN-999`   |  500 | Uncaught exception — details scrubbed before the response leaves the JVM                  |

## Order Service (`FTGO-ORD-*`)

| Code             | HTTP | Exception                                                      |
| ---------------- | ---: | -------------------------------------------------------------- |
| `FTGO-ORD-001`   |  404 | `OrderNotFoundException` (extends `EntityNotFoundException`)   |
| `FTGO-ORD-002`   |  409 | `UnsupportedStateTransitionException` (extends `ConflictException`) |
| `FTGO-ORD-003`   |  422 | `OrderMinimumNotMetException` (extends `BusinessRuleViolationException`) |
| `FTGO-ORD-004`   |  422 | `InvalidMenuItemIdException` (extends `BusinessRuleViolationException`) |

## Consumer Service (`FTGO-CON-*`)

| Code             | HTTP | Exception                                                      |
| ---------------- | ---: | -------------------------------------------------------------- |
| `FTGO-CON-001`   |  404 | `ConsumerNotFoundException` (extends `EntityNotFoundException`) |

## Restaurant Service (`FTGO-RES-*`)

| Code             | HTTP | Exception                                                          |
| ---------------- | ---: | ------------------------------------------------------------------ |
| `FTGO-RES-001`   |  404 | `RestaurantNotFoundException` (extends `EntityNotFoundException`)  |

## Courier Service (`FTGO-CRR-*`)

| Code             | HTTP | Exception                                                     |
| ---------------- | ---: | ------------------------------------------------------------- |
| `FTGO-CRR-001`   |  404 | `CourierNotFoundException` (extends `EntityNotFoundException`) |

## HTTP Status Mapping

| Status | Semantics                                  | Emitted by                                                     |
| -----: | ------------------------------------------ | -------------------------------------------------------------- |
|  400   | Bad request / validation failure           | `FTGO-GEN-001` … `FTGO-GEN-004`                                |
|  401   | Authentication required                    | `FTGO-GEN-006` (reserved)                                      |
|  403   | Forbidden                                  | `FTGO-GEN-007` (reserved)                                      |
|  404   | Entity not found                           | `FTGO-ORD-001`, `FTGO-CON-001`, `FTGO-RES-001`, `FTGO-CRR-001` |
|  405   | Method not allowed                         | `FTGO-GEN-005`                                                 |
|  409   | Conflict with current state                | `FTGO-ORD-002`, `FTGO-GEN-008`                                 |
|  422   | Business rule violation                    | `FTGO-ORD-003`, `FTGO-ORD-004`                                 |
|  500   | Unhandled / internal error                 | `FTGO-GEN-999`                                                 |
|  502   | Downstream HTTP error (bad gateway)        | `FTGO-GEN-009` via `HttpStatusCodeException`, `RestClientException` |
|  503   | Downstream unreachable / timed out         | `FTGO-GEN-009` via `ResourceAccessException`, `ServiceUnavailableException` |

## Adding a New Code

1. Add the constant to the [`ErrorCode` enum](../ftgo-common/src/main/java/net/chrisrichardson/ftgo/common/errors/ErrorCode.java).
   Use the next free numeric suffix in your domain (`FTGO-ORD-005`, …); never
   reuse a retired suffix.
2. If the code maps to a new HTTP status, add a branch to
   [`GlobalExceptionHandler`](../ftgo-common/src/main/java/net/chrisrichardson/ftgo/common/errors/GlobalExceptionHandler.java)
   (or a service-specific subclass) that throws an appropriate subtype of
   `FtgoException`.
3. Update this document with a new row under the right domain section.
4. Add or extend a test in `ftgo-common` / the owning service to lock the
   code + status pair.
