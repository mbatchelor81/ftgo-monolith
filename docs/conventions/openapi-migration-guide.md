# Springfox to SpringDoc OpenAPI 3 Migration Guide

## Overview

The FTGO platform is migrating from **Springfox Swagger 2.x** (deprecated,
unmaintained) to **SpringDoc OpenAPI 3.0**. This guide covers the shared library,
annotation mapping, and per-service migration steps.

---

## Architecture

```
common-swagger/          ← Legacy (Springfox 2.8.0) — retained, not modified
libs/ftgo-openapi-lib/   ← New (SpringDoc 2.5.0 / OpenAPI 3.0)
```

Legacy monolith modules continue to use `common-swagger`.  
New microservices (`services/*`) use `ftgo-openapi-lib`.

---

## Dependency Changes

### Before (Springfox)

```groovy
dependencies {
    implementation project(":common-swagger")
    // Pulls in: io.springfox:springfox-swagger2:2.8.0
    //           io.springfox:springfox-swagger-ui:2.8.0
}
```

### After (SpringDoc)

```groovy
dependencies {
    implementation project(":ftgo-openapi-lib")
    // Pulls in: org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0
}
```

---

## Annotation Mapping

| Springfox (Swagger 2) | SpringDoc (OpenAPI 3) |
|-----------------------|-----------------------|
| `@Api(tags = "Orders")` | `@Tag(name = "Orders")` |
| `@ApiOperation(value = "...")` | `@Operation(summary = "...")` |
| `@ApiParam` | `@Parameter` |
| `@ApiResponse(code = 200, message = "...")` | `@ApiResponse(responseCode = "200", description = "...")` |
| `@ApiModel` | `@Schema` |
| `@ApiModelProperty` | `@Schema(description = "...")` |
| `@ApiIgnore` | `@Hidden` |
| `@EnableSwagger2` | (not needed — auto-configured) |
| `Docket` bean | `GroupedOpenApi` bean or `OpenAPI` bean |

### Import Package Changes

| Old | New |
|-----|-----|
| `io.swagger.annotations.*` | `io.swagger.v3.oas.annotations.*` |
| `springfox.documentation.*` | `org.springdoc.core.*` |

---

## Migration Steps per Service

### 1. Update `build.gradle`

Replace the `common-swagger` dependency:

```diff
 dependencies {
-    implementation project(":common-swagger")
+    implementation project(":ftgo-openapi-lib")
 }
```

### 2. Remove Springfox Configuration

Delete any `@EnableSwagger2` annotations and `Docket` bean definitions.
The `ftgo-openapi-lib` auto-configuration handles everything.

### 3. Update Controller Annotations

```java
// Before (Springfox)
@Api(tags = "Orders")
@RestController
@RequestMapping("/orders")
public class OrderController {

    @ApiOperation(value = "Get order by ID")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Order found"),
        @ApiResponse(code = 404, message = "Order not found")
    })
    @GetMapping("/{orderId}")
    public Order getOrder(@ApiParam("Order ID") @PathVariable long orderId) { ... }
}

// After (SpringDoc / OpenAPI 3)
@Tag(name = "Orders", description = "Order lifecycle management")
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Operation(summary = "Get order by ID")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/{orderId}")
    public Order getOrder(@Parameter(description = "Order ID") @PathVariable long orderId) { ... }
}
```

### 4. Update DTOs (Optional but Recommended)

```java
// Before
@ApiModel(description = "Create order request")
public class CreateOrderRequest {
    @ApiModelProperty(value = "Consumer ID", required = true)
    private Long consumerId;
}

// After
@Schema(description = "Create order request")
public class CreateOrderRequest {
    @Schema(description = "Consumer ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long consumerId;
}
```

### 5. Configure Service Metadata

Add to `application.yml`:

```yaml
ftgo:
  openapi:
    title: Order Service
    description: Manages the order lifecycle
    version: v1
```

### 6. Verify

Start the service and navigate to:
- `http://localhost:8080/swagger-ui.html` — Swagger UI
- `http://localhost:8080/v3/api-docs` — OpenAPI JSON spec

---

## Swagger UI URL Changes

| Springfox | SpringDoc |
|-----------|-----------|
| `/swagger-ui.html` (static page) | `/swagger-ui.html` (redirect to `/swagger-ui/index.html`) |
| `/v2/api-docs` | `/v3/api-docs` |

The Swagger UI URL remains `/swagger-ui.html` for backward compatibility —
SpringDoc handles the redirect automatically.

---

## Customization

### Per-Service Grouped APIs

Override the default `GroupedOpenApi` bean:

```java
@Bean
public GroupedOpenApi ordersApi() {
    return GroupedOpenApi.builder()
            .group("orders")
            .pathsToMatch("/api/v1/orders/**")
            .build();
}
```

### Custom OpenAPI Bean

Override the default `OpenAPI` bean:

```java
@Bean
public OpenAPI customOpenApi() {
    return new OpenAPI()
            .info(new Info()
                    .title("My Custom Service")
                    .version("v2"));
}
```
