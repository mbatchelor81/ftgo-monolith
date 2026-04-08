# FTGO REST API Standards

> **Jira**: EM-45 — Define REST API Standards and Migrate to SpringDoc OpenAPI 3

This document defines the REST API conventions for all FTGO microservices.
All new and migrated endpoints **must** follow these standards.

---

## 1. API Versioning

| Strategy | Convention |
|----------|-----------|
| Scheme | URL path prefix |
| Format | `/api/v{major}/...` |
| Current | `/api/v1/...` |

- The version number is the **major** version only.
- Breaking changes require a new major version (`/api/v2/...`).
- Non-breaking additions (new fields, new optional query params) do **not** require a version bump.

### Examples

```
GET  /api/v1/orders
POST /api/v1/orders
GET  /api/v1/orders/{orderId}
```

---

## 2. URL Naming Conventions

| Rule | Convention | Example |
|------|-----------|---------|
| Resource names | Plural nouns, lowercase, kebab-case | `/api/v1/orders`, `/api/v1/menu-items` |
| Path parameters | camelCase | `/api/v1/orders/{orderId}` |
| Query parameters | camelCase | `?pageSize=20&sortBy=createdAt` |
| Nested resources | Parent/child relationship | `/api/v1/restaurants/{restaurantId}/menu-items` |
| Actions (non-CRUD) | Use sub-resource verb | `POST /api/v1/orders/{orderId}/cancel` |

### Do

- `GET /api/v1/consumers`
- `GET /api/v1/consumers/{consumerId}/orders`
- `POST /api/v1/orders/{orderId}/accept`

### Don't

- `GET /api/v1/getConsumers` — verbs in the path
- `GET /api/v1/Consumer` — singular or PascalCase
- `GET /api/v1/consumer_orders` — underscores

---

## 3. HTTP Methods and Status Codes

### Methods

| Method | Semantics | Idempotent |
|--------|-----------|------------|
| `GET` | Retrieve a resource or collection | Yes |
| `POST` | Create a new resource or trigger an action | No |
| `PUT` | Full replacement of an existing resource | Yes |
| `PATCH` | Partial update of an existing resource | No |
| `DELETE` | Remove a resource | Yes |

### Success Status Codes

| Code | When to use |
|------|------------|
| `200 OK` | Successful GET, PUT, PATCH, or action POST |
| `201 Created` | Successful resource creation (POST). Include `Location` header |
| `204 No Content` | Successful DELETE or update with no response body |

### Error Status Codes

| Code | When to use |
|------|------------|
| `400 Bad Request` | Validation failure, malformed request body |
| `401 Unauthorized` | Missing or invalid authentication |
| `403 Forbidden` | Authenticated but insufficient permissions |
| `404 Not Found` | Resource does not exist |
| `409 Conflict` | State conflict (e.g., cancelling a delivered order) |
| `422 Unprocessable Entity` | Semantic validation error (valid JSON but business rule violated) |
| `500 Internal Server Error` | Unexpected server error |

---

## 4. Request and Response Format

### Content Type

All endpoints produce and consume `application/json`.

### Error Response Body

All error responses **must** use this standard envelope:

```json
{
  "timestamp": "2025-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Order total must be greater than zero",
  "path": "/api/v1/orders"
}
```

### Successful Collection Response

Collection endpoints return a JSON array or a paginated wrapper (see §5):

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 142,
  "totalPages": 8
}
```

---

## 5. Pagination

Use **offset-based** pagination with the following query parameters:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Zero-based page index |
| `size` | `20` | Number of items per page (max `100`) |
| `sort` | — | Sort field and direction, e.g. `createdAt,desc` |

Spring Data's `Pageable` maps directly to these parameters.

---

## 6. Date and Time Format

| Rule | Format |
|------|--------|
| All timestamps | ISO 8601: `yyyy-MM-dd'T'HH:mm:ss'Z'` |
| Time zone | Always **UTC** |
| Jackson config | `jackson-datatype-jsr310` with `WRITE_DATES_AS_TIMESTAMPS = false` |

### Example

```json
{
  "createdAt": "2025-01-15T10:30:00Z",
  "deliveryTime": "2025-01-15T11:15:00Z"
}
```

---

## 7. OpenAPI / Swagger UI

Each microservice exposes OpenAPI documentation via SpringDoc:

| Item | URL |
|------|-----|
| Swagger UI | `/swagger-ui.html` |
| OpenAPI JSON | `/v3/api-docs` |
| OpenAPI YAML | `/v3/api-docs.yaml` |

### Annotation Requirements

Every controller **must** have:

- `@Tag(name = "...", description = "...")` on the controller class.
- `@Operation(summary = "...")` on each handler method.
- `@ApiResponse` annotations for success and expected error codes.
- `@Parameter` on path/query parameters when the name alone is not self-explanatory.
- `@Schema` on request/response DTOs with `description` and `example` values.

### Example

```java
@Tag(name = "Orders", description = "Order lifecycle management")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Operation(summary = "Create a new order")
    @ApiResponse(responseCode = "201", description = "Order created")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        // ...
    }
}
```

---

## 8. Shared OpenAPI Configuration

The `shared:common-swagger` module provides a reusable `OpenApiConfiguration` bean
that sets default metadata (title, version, contact, license). Each service
customises the title and description via Spring properties:

```yaml
ftgo:
  api:
    title: FTGO Order Service
    description: Manages order lifecycle — creation, acceptance, preparation, and delivery
    version: 1.0.0
```

Services include the shared module as a Gradle dependency:

```groovy
implementation project(":shared:common-swagger")
```

---

## 9. Change Log

| Date | Author | Description |
|------|--------|-------------|
| 2025-01-15 | FTGO Engineering | Initial version — EM-45 |
