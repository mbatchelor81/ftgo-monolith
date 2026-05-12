# FTGO REST API Standards

## Overview

This document defines the REST API conventions for all FTGO platform services.
All new microservices **must** follow these standards. Legacy monolith endpoints
will be migrated to comply during the incremental service extraction.

---

## 1. URL Naming Conventions

### Base Path

All API endpoints are served under the root context path (`/`).  
Individual services may configure a context path via `server.servlet.context-path`.

### Resource Naming

| Rule | Example |
|------|---------|
| Use lowercase, hyphen-separated nouns | `/orders`, `/restaurant-menus` |
| Use plural nouns for collections | `/consumers`, `/orders` |
| Use path parameters for individual resources | `/orders/{orderId}` |
| Nest sub-resources one level deep at most | `/orders/{orderId}/line-items` |
| Avoid verbs in URLs — use HTTP methods instead | `POST /orders` not `/createOrder` |
| Avoid file extensions | `/orders` not `/orders.json` |

### Query Parameters

| Use Case | Convention | Example |
|----------|-----------|---------|
| Filtering | Field-based query params | `?status=APPROVED&restaurantId=42` |
| Sorting | `sort` param with field and direction | `?sort=createdAt,desc` |
| Pagination | `page` (0-based) and `size` | `?page=0&size=20` |
| Searching | `q` for free-text search | `?q=pizza` |

---

## 2. HTTP Methods

| Method | Usage | Idempotent | Safe |
|--------|-------|------------|------|
| `GET` | Retrieve a resource or collection | Yes | Yes |
| `POST` | Create a new resource | No | No |
| `PUT` | Full replacement of a resource | Yes | No |
| `PATCH` | Partial update of a resource | No | No |
| `DELETE` | Remove a resource | Yes | No |

### Method Rules

- `POST` returns `201 Created` with a `Location` header pointing to the new resource.
- `PUT` and `PATCH` return `200 OK` with the updated resource.
- `DELETE` returns `204 No Content` on success.
- `GET` returns `200 OK` for single resources and paginated collections.

---

## 3. HTTP Status Codes

### Success

| Code | Usage |
|------|-------|
| `200 OK` | Successful read or update |
| `201 Created` | Resource created; include `Location` header |
| `204 No Content` | Successful delete or action with no response body |

### Client Error

| Code | Usage |
|------|-------|
| `400 Bad Request` | Validation failure, malformed input |
| `401 Unauthorized` | Missing or invalid authentication |
| `403 Forbidden` | Authenticated but insufficient permissions |
| `404 Not Found` | Resource does not exist |
| `409 Conflict` | State conflict (e.g., duplicate creation, concurrent update) |
| `422 Unprocessable Entity` | Semantically invalid input (business rule violation) |

### Server Error

| Code | Usage |
|------|-------|
| `500 Internal Server Error` | Unhandled server-side error |
| `503 Service Unavailable` | Downstream dependency failure or maintenance |

---

## 4. Request / Response Envelope

### Successful Single-Resource Response

```json
{
  "data": {
    "id": 42,
    "status": "APPROVED",
    "createdAt": "2024-06-15T10:30:00Z"
  }
}
```

### Successful Collection Response (Paginated)

```json
{
  "data": [
    { "id": 1, "name": "Ajanta" },
    { "id": 2, "name": "Dosa Palace" }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 47,
    "totalPages": 3
  }
}
```

### Error Response

```json
{
  "error": {
    "code": "ORDER_NOT_FOUND",
    "message": "Order with id 99 does not exist.",
    "status": 404,
    "timestamp": "2024-06-15T10:30:00Z",
    "path": "/orders/99",
    "details": []
  }
}
```

### Validation Error Response (400 / 422)

```json
{
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "Request validation failed.",
    "status": 400,
    "timestamp": "2024-06-15T10:30:00Z",
    "path": "/orders",
    "details": [
      { "field": "consumerId", "message": "must not be null" },
      { "field": "lineItems", "message": "must contain at least 1 item" }
    ]
  }
}
```

---

## 5. Pagination

### Request Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | `0` | Zero-based page index |
| `size` | int | `20` | Number of items per page (max 100) |
| `sort` | string | — | Comma-separated field and direction, e.g. `createdAt,desc` |

### Response Metadata

Include a `page` object in all paginated responses:

```json
{
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 47,
    "totalPages": 3
  }
}
```

Services use Spring Data's `Pageable` for pagination support.

---

## 6. Date and Time Format

All date/time values **must** use [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601)
format in UTC:

```
2024-06-15T10:30:00Z
```

- Jackson's `JavaTimeModule` is enabled by default.
- Configure `spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ss'Z'` or rely
  on `@JsonFormat` annotations for custom formats.
- Date-only fields use `yyyy-MM-dd` (e.g., `2024-06-15`).

---

## 7. API Versioning Strategy

### Approach: URL Path Versioning

API versions are specified in the URL path:

```
/api/v1/orders
/api/v2/orders
```

### Rules

1. **Start with `v1`** — all new endpoints are versioned from day one.
2. **Increment the major version** only for breaking changes:
   - Removing a field from a response
   - Changing the type of an existing field
   - Removing or renaming an endpoint
3. **Non-breaking changes** do not require a new version:
   - Adding optional fields to a request or response
   - Adding new endpoints
   - Adding new query parameters
4. **Support at most two concurrent versions** (`vN` and `vN-1`). Deprecate
   the older version with a minimum 6-month notice period.
5. **Deprecation header**: When a version is deprecated, include:
   ```
   Deprecation: true
   Sunset: Sat, 15 Jun 2025 00:00:00 GMT
   Link: </api/v2/orders>; rel="successor-version"
   ```

### Controller Pattern

```java
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order lifecycle management")
public class OrderController {
    // ...
}
```

---

## 8. OpenAPI Documentation

### Library

All new microservices use **SpringDoc OpenAPI** (`springdoc-openapi-starter-webmvc-ui`)
provided by the `ftgo-openapi-lib` shared library.

### Access Points

| Path | Description |
|------|-------------|
| `/swagger-ui.html` | Interactive Swagger UI |
| `/v3/api-docs` | OpenAPI 3.0 JSON spec |
| `/v3/api-docs.yaml` | OpenAPI 3.0 YAML spec |

### Required Annotations

All controller endpoints **must** include at minimum:

```java
@Operation(summary = "Create a new order")
@ApiResponse(responseCode = "201", description = "Order created")
@ApiResponse(responseCode = "400", description = "Validation failed")
```

### Configuration

Add the shared library as a dependency:

```groovy
dependencies {
    implementation project(":ftgo-openapi-lib")
}
```

Customize per-service metadata in `application.yml`:

```yaml
ftgo:
  openapi:
    title: Order Service
    description: Manages the order lifecycle
    version: v1
```

---

## 9. Content Negotiation

- Default content type: `application/json`.
- `Content-Type` header is required for `POST`, `PUT`, and `PATCH` requests.
- `Accept` header should be `application/json` (or omitted for the default).
- Error responses always use `application/json`.

---

## 10. Naming Summary

| Element | Convention | Example |
|---------|-----------|---------|
| URL path segments | lowercase, hyphen-separated | `/order-line-items` |
| Query parameters | camelCase | `?restaurantId=42` |
| JSON field names | camelCase | `"orderId": 42` |
| Enum values in JSON | UPPER_SNAKE_CASE | `"status": "APPROVED"` |
| HTTP headers | Title-Case | `Content-Type`, `X-Request-Id` |
