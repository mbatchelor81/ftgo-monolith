# FTGO REST API Standards

> Version 1.0 — Applies to all FTGO microservices under `services/`.

---

## 1. API Versioning

### Strategy: URI Path Versioning

All endpoints are prefixed with `/api/v{major}`:

```
GET /api/v1/orders
POST /api/v1/consumers
```

**Rules:**
- The version number reflects the **major** version only.
- The current version is **v1**.
- A new major version is introduced only for **breaking changes** (removed fields, renamed endpoints, changed semantics).
- Non-breaking changes (new optional fields, new endpoints) do **not** require a version bump.
- At most **two** major versions may be active simultaneously. The older version must include a `Sunset` header indicating its deprecation date.

### Deprecation Headers

When an endpoint or version is deprecated, responses must include:

```
Deprecation: true
Sunset: Sat, 01 Mar 2025 00:00:00 GMT
Link: </api/v2/orders>; rel="successor-version"
```

---

## 2. URL Conventions

### Resource Naming

| Rule | Example |
|------|---------|
| Use **plural nouns** for collections | `/api/v1/orders`, `/api/v1/consumers` |
| Use **kebab-case** for multi-word resources | `/api/v1/menu-items` |
| Nest sub-resources one level deep maximum | `/api/v1/orders/{orderId}/line-items` |
| Use path parameters for identity | `/api/v1/orders/{orderId}` |
| Use query parameters for filtering/sorting | `/api/v1/orders?status=APPROVED&sort=createdAt,desc` |

### HTTP Methods

| Method | Semantics | Idempotent | Safe |
|--------|-----------|:----------:|:----:|
| `GET` | Read a resource or collection | Yes | Yes |
| `POST` | Create a new resource | No | No |
| `PUT` | Full replacement of a resource | Yes | No |
| `PATCH` | Partial update of a resource | No | No |
| `DELETE` | Remove a resource | Yes | No |

---

## 3. Request Format

### Headers

All requests must include:

| Header | Required | Description |
|--------|:--------:|-------------|
| `Content-Type` | Yes (for request bodies) | `application/json` |
| `Accept` | Recommended | `application/json` |

### Request Bodies

- Use **camelCase** for JSON field names.
- All fields must be validated using Bean Validation annotations (`@NotNull`, `@Size`, `@Valid`, etc.).
- Request DTOs live in the service's `-api` module or in the service itself.

**Example: Create Consumer Request**

```json
{
  "firstName": "John",
  "lastName": "Doe"
}
```

---

## 4. Response Format

### Success Responses

#### Single Resource

```json
{
  "id": 123,
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### Paginated Collection

All list endpoints returning variable-length results must support pagination.

```json
{
  "content": [
    { "id": 1, "firstName": "John" },
    { "id": 2, "firstName": "Jane" }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

**Pagination Query Parameters:**

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Zero-based page number |
| `size` | `20` | Items per page (max: 100) |
| `sort` | service-defined | Field and direction, e.g. `createdAt,desc` |

Use `com.ftgo.openapi.model.PagedResponse<T>` from `ftgo-openapi-lib`.

### HTTP Status Codes

| Code | When to Use |
|------|-------------|
| `200 OK` | Successful `GET`, `PUT`, `PATCH`, `DELETE` |
| `201 Created` | Successful `POST` that creates a resource. Include `Location` header. |
| `204 No Content` | Successful `DELETE` with no response body |
| `400 Bad Request` | Malformed request or validation failure |
| `404 Not Found` | Resource does not exist |
| `409 Conflict` | Business rule conflict (e.g., invalid state transition) |
| `422 Unprocessable Entity` | Request is syntactically valid but semantically incorrect |
| `500 Internal Server Error` | Unexpected server failure |

### Error Response Format

All error responses use `com.ftgo.openapi.model.ApiErrorResponse` from `ftgo-openapi-lib`:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/orders",
  "timestamp": "2024-01-15T10:30:00Z",
  "fieldErrors": [
    {
      "field": "quantity",
      "message": "must be greater than 0",
      "rejectedValue": -1
    }
  ]
}
```

The `fieldErrors` array is only present for validation failures.

---

## 5. OpenAPI Documentation

### SpringDoc Configuration

All services use **SpringDoc OpenAPI 3** via the shared `ftgo-openapi-lib` module. This replaces the deprecated Springfox/Swagger 2.x library.

**Dependency:**

```groovy
implementation project(':ftgo-openapi-lib')
```

**Swagger UI** is available at:

```
http://<host>:<port>/swagger-ui.html
```

**OpenAPI JSON spec** is available at:

```
http://<host>:<port>/v3/api-docs
```

### Per-Service Configuration

Each service customizes its documentation via `application.yml`:

```yaml
ftgo:
  openapi:
    title: FTGO Consumer Service API
    description: Manages consumer registration and validation
    version: v1
    contact-name: FTGO Platform Team
    contact-email: platform@ftgo.com

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method
    tags-sorter: alpha
```

### OpenAPI Annotations

All controllers must be annotated with OpenAPI 3 annotations:

```java
@RestController
@RequestMapping("/api/v1/consumers")
@Tag(name = "Consumers", description = "Consumer management operations")
public class ConsumerController {

    @Operation(summary = "Create a new consumer",
               description = "Registers a new consumer in the system")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Consumer created"),
        @ApiResponse(responseCode = "400", description = "Validation error",
                     content = @Content(schema = @Schema(
                         implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ConsumerResponse> createConsumer(
            @Valid @RequestBody CreateConsumerRequest request) {
        // ...
    }
}
```

**Required annotations per endpoint:**

| Annotation | Purpose |
|-----------|---------|
| `@Tag` | Groups endpoints in Swagger UI (class-level) |
| `@Operation` | Summary and description for each endpoint |
| `@ApiResponse` | Documents each possible response code |
| `@Schema` | Documents request/response model fields |
| `@Parameter` | Documents path/query parameters |

---

## 6. Naming Conventions

### DTO Naming

| Type | Convention | Example |
|------|-----------|---------|
| Create request | `Create{Entity}Request` | `CreateConsumerRequest` |
| Update request | `Update{Entity}Request` | `UpdateOrderRequest` |
| Patch/revise request | `Revise{Entity}Request` | `ReviseOrderRequest` |
| Single response | `{Entity}Response` | `ConsumerResponse` |
| Collection response | `PagedResponse<{Entity}Response>` | `PagedResponse<OrderResponse>` |

### Controller Naming

- Class: `{Entity}Controller`
- Base path: `/api/v1/{entities}` (plural, kebab-case)

### Service Naming

- Interface: `{Entity}Service`
- Implementation: `{Entity}ServiceImpl` (only if an interface is needed)

---

## 7. API Contract Testing

### Strategy

Each service should include integration tests that verify:

1. **Request validation** — Invalid requests return `400` with `ApiErrorResponse`.
2. **Response schema** — Responses match the documented OpenAPI schema.
3. **Status codes** — Each endpoint returns the documented HTTP status codes.

### Tools

- **Spring MockMvc** for in-process controller tests.
- **REST Assured** for service-level integration tests.
- **OpenAPI spec validation** via SpringDoc-generated JSON at `/v3/api-docs`.

### Example Contract Test

```java
@WebMvcTest(ConsumerController.class)
class ConsumerControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createConsumer_withInvalidBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/consumers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
```

---

## 8. Migration from Springfox to SpringDoc

### What Changed

| Aspect | Springfox (old) | SpringDoc (new) |
|--------|----------------|-----------------|
| Spec version | Swagger 2.0 | OpenAPI 3.0 |
| Library | `springfox-swagger2` 2.8.0 | `springdoc-openapi-starter-webmvc-ui` 2.5.0 |
| Configuration | `Docket` bean + `@EnableSwagger2` | `OpenAPI` bean (auto-configured) |
| Annotations | `io.swagger.annotations.*` | `io.swagger.v3.oas.annotations.*` |
| UI path | `/swagger-ui.html` (static) | `/swagger-ui.html` (configurable) |
| Spec endpoint | `/v2/api-docs` | `/v3/api-docs` |
| Module | `common-swagger` (monolith) | `ftgo-openapi-lib` (services/) |

### Migration Checklist

1. Add `implementation project(':ftgo-openapi-lib')` to the service `build.gradle`.
2. Remove any Springfox dependencies.
3. Replace `@Api` with `@Tag`.
4. Replace `@ApiOperation` with `@Operation`.
5. Replace `@ApiParam` with `@Parameter`.
6. Replace `@ApiModel` / `@ApiModelProperty` with `@Schema`.
7. Remove `@EnableSwagger2` — SpringDoc auto-configures via Spring Boot starters.
8. Configure service-specific metadata in `application.yml` under `ftgo.openapi.*`.

> **Note:** The monolith's `common-swagger` module is left unchanged. It will be removed in a future phase when the monolith is fully decomposed.

---

## Appendix: Quick Reference

### Swagger UI URLs (per service)

| Service | Default Port | Swagger UI |
|---------|:------------:|------------|
| Consumer Service | 8082 | `http://localhost:8082/swagger-ui.html` |
| Order Service | 8083 | `http://localhost:8083/swagger-ui.html` |
| Restaurant Service | 8084 | `http://localhost:8084/swagger-ui.html` |
| Courier Service | 8085 | `http://localhost:8085/swagger-ui.html` |
