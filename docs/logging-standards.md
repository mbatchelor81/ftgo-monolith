# FTGO Logging Standards

> **Status:** Approved
> **Applies to:** All FTGO microservices
> **Dependencies:** `shared:ftgo-logging-lib`, `shared:ftgo-tracing-lib`

---

## 1. Log Level Guidelines

| Level   | When to Use | Examples |
|---------|-------------|----------|
| `ERROR` | Unrecoverable failures that require immediate attention. The current operation cannot complete. | Unhandled exceptions, database connection failures, external service unavailable after retries, data corruption detected. |
| `WARN`  | Unexpected conditions that the system can recover from, but that may indicate a problem. | Deprecated API usage, fallback activated, retry attempt, approaching resource limits, slow query detected. |
| `INFO`  | Key business events and operational milestones. This is the default level for production. | Service started/stopped, order created/cancelled, payment processed, user authenticated, configuration loaded. |
| `DEBUG` | Detailed diagnostic information useful during development and troubleshooting. | Method entry/exit with parameters, SQL queries, cache hits/misses, request/response summaries, state transitions. |
| `TRACE` | Very fine-grained diagnostic output. Rarely enabled outside of local development. | Full request/response bodies (non-production only), loop iterations, field-level data transformations. |

### Rules

- Production services run at `INFO` level by default.
- Never log at `ERROR` for expected business conditions (e.g., validation failures are `WARN` or `INFO`).
- Use `DEBUG` for anything that would be noisy at `INFO` but valuable when diagnosing issues.
- `TRACE` must **never** be enabled in production.

---

## 2. What to Log

### Always Log

- **Service lifecycle events:** startup, shutdown, configuration loaded.
- **Business events:** order created, payment processed, courier assigned, restaurant accepted.
- **Inbound requests:** HTTP method, URI, response status, duration (at `INFO` or `DEBUG`).
- **External service calls:** target service, operation, duration, success/failure.
- **Errors and exceptions:** full stack trace at `ERROR`, with contextual MDC fields.
- **Security events:** authentication success/failure, authorization denied, token refresh.
- **State transitions:** order state changes, courier availability changes.

### Never Log

- **Personally Identifiable Information (PII):** email addresses, phone numbers, physical addresses, full names in combination with identifiers.
- **Credentials:** passwords, API keys, tokens (JWT, OAuth), secret keys.
- **Financial data:** full credit card numbers, bank account numbers, CVVs.
- **Full request/response bodies in production:** these may contain PII or large payloads.
- **Health check requests:** suppress `/actuator/health` and similar endpoints from access logs to reduce noise.

### Masking

The `ftgo-logging-lib` provides a `MaskingConverter` that automatically redacts:
- Credit card numbers (replaced with masked format, e.g., `****-****-****-1234`)
- Passwords and secrets in key-value patterns
- Bearer tokens and Authorization headers

See [Section 6: Sensitive Data Masking](#6-sensitive-data-masking) for configuration details.

---

## 3. Structured Logging Format

All services use **structured JSON logging** in deployed environments (dev, staging, production) via the Logstash Logback Encoder. Local development uses a human-readable plain-text format.

### JSON Log Fields

| Field           | Source                  | Description |
|-----------------|------------------------|-------------|
| `@timestamp`    | Logback                | ISO-8601 UTC timestamp |
| `level`         | Logback                | Log level (ERROR, WARN, INFO, DEBUG, TRACE) |
| `logger_name`   | Logback                | Logger name (shortened) |
| `message`       | Application            | Log message |
| `thread_name`   | Logback                | Thread name |
| `service`       | `spring.application.name` | Microservice name |
| `traceId`       | MDC (Micrometer/Brave) | Distributed trace ID |
| `spanId`        | MDC (Micrometer/Brave) | Current span ID |
| `correlationId` | MDC (CorrelationIdFilter) | Request correlation ID |
| `userId`        | MDC (LogContext)       | Authenticated user ID |
| `requestId`     | MDC (LogContext)       | Unique request identifier |
| `requestMethod` | MDC (CorrelationIdFilter) | HTTP method |
| `requestUri`    | MDC (CorrelationIdFilter) | Request URI |
| `stack_trace`   | Logback                | Exception stack trace (when present) |

### Example JSON Output

```json
{
  "@timestamp": "2026-04-08T12:34:56.789Z",
  "level": "INFO",
  "logger_name": "c.f.o.domain.OrderService",
  "message": "Order created successfully",
  "thread_name": "http-nio-8080-exec-1",
  "service": "ftgo-order-service",
  "traceId": "abc123def456",
  "spanId": "789ghi012",
  "correlationId": "req-550e8400-e29b",
  "userId": "consumer-42",
  "requestId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Local Development Format

```
2026-04-08 12:34:56.789 [http-nio-8080-exec-1] INFO  c.f.o.domain.OrderService [req-550e] [abc123] [consumer-42] - Order created successfully
```

---

## 4. MDC (Mapped Diagnostic Context) Fields

All log entries automatically include the following MDC fields:

| MDC Key          | Set By                     | Description |
|------------------|----------------------------|-------------|
| `correlationId`  | `CorrelationIdFilter`      | From `X-Correlation-ID` header or auto-generated UUID |
| `requestId`      | `CorrelationIdFilter`      | Unique per-request identifier |
| `requestMethod`  | `CorrelationIdFilter`      | HTTP method (GET, POST, etc.) |
| `requestUri`     | `CorrelationIdFilter`      | Request URI path |
| `userId`         | `CorrelationIdFilter`      | From `X-User-ID` header (set by API Gateway after authentication) |
| `traceId`        | Micrometer Tracing (Brave) | Distributed trace ID (from `ftgo-tracing-lib`) |
| `spanId`         | Micrometer Tracing (Brave) | Current span ID (from `ftgo-tracing-lib`) |
| `serviceName`    | `LogContext`               | Service name (from `spring.application.name`) |

### Setting MDC Programmatically

Use the `LogContext` utility for manual MDC management (e.g., in async tasks or message consumers):

```java
import com.ftgo.logging.mdc.LogContext;

// Set fields
LogContext.setUserId("consumer-42");
LogContext.setRequestId("req-123");

// Clear when done (typically in a finally block)
LogContext.clear();
```

---

## 5. Per-Environment Log Level Configuration

### Local Development (`local` profile)

| Package | Level | Rationale |
|---------|-------|-----------|
| Root    | `INFO`  | Baseline for all loggers |
| `com.ftgo` | `DEBUG` | Full diagnostic output for application code |
| Frameworks | Default (`INFO`) | Standard framework output |

Output: **Plain-text** console + **rolling file** (`logs/app.log`, 10 MB max, 3 files retained)

### Dev / Staging (`dev`, `staging` profiles)

| Package | Level | Rationale |
|---------|-------|-----------|
| Root    | `INFO`  | Baseline |
| `com.ftgo` | `INFO` | Business events and key operations |
| `org.hibernate` | `WARN` | Suppress noisy SQL logging |
| `org.springframework` | `WARN` | Suppress framework noise |

Output: **Structured JSON** via async appender

### Production (`prod` profile)

| Package | Level | Rationale |
|---------|-------|-----------|
| Root    | `WARN`  | Only warnings and errors from third-party libraries |
| `com.ftgo` | `INFO` | Business events and operational data |
| `org.hibernate` | `ERROR` | Only database errors |
| `org.springframework` | `ERROR` | Only framework errors |

Output: **Structured JSON** via async appender

---

## 6. Sensitive Data Masking

The `MaskingConverter` in `ftgo-logging-lib` is registered as a custom Logback converter and automatically redacts sensitive patterns in log messages.

### Masked Patterns

| Pattern | Example Input | Masked Output |
|---------|--------------|---------------|
| Credit card numbers | `4111111111111111` | `************1111` |
| Password fields | `"password":"secret123"` | `"password":"******"` |
| Bearer tokens | `Bearer eyJhbGciOi...` | `Bearer [REDACTED]` |
| Authorization headers | `Authorization: Basic abc` | `Authorization: [REDACTED]` |

### Enabling the Masking Converter

The masking converter is automatically registered when using the shared `ftgo-logback-defaults.xml`. It is applied to both JSON and plain-text appenders.

No service-specific configuration is required.

---

## 7. Logging Aspect (Method Entry/Exit)

The `LoggingAspect` in `ftgo-logging-lib` provides automatic method entry/exit logging for service layer methods. It is configurable per package via Spring properties.

### Configuration

```yaml
ftgo:
  logging:
    aspect:
      enabled: true                          # Enable/disable the aspect (default: true)
      base-packages:                         # Packages to instrument
        - com.ftgo.orderservice.domain
        - com.ftgo.consumerservice.domain
      log-level: DEBUG                       # Level for entry/exit logs (default: DEBUG)
      include-args: true                     # Log method arguments (default: true)
      include-result: false                  # Log return values (default: false)
      slow-execution-threshold-ms: 500       # Warn if method takes longer (default: 500)
```

### Example Output

```
DEBUG c.f.o.d.OrderService - --> createOrder(consumerId=42, restaurantId=7, lineItems=[...])
DEBUG c.f.o.d.OrderService - <-- createOrder completed in 45ms
WARN  c.f.o.d.OrderService - <-- processPayment completed in 1250ms [SLOW]
```

---

## 8. Shared Configuration Files

### logback-spring.xml (per service)

Each service includes the shared defaults and overrides only what is service-specific:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Import shared FTGO logging configuration (JSON + plain appenders, masking, per-env levels) -->
    <include resource="ftgo-logback-defaults.xml"/>
    <include resource="ftgo-logback-appenders.xml"/>
    <include resource="ftgo-logback-profiles.xml"/>

    <!-- Service-specific overrides (optional) -->
    <!-- <logger name="com.ftgo.orderservice.domain" level="DEBUG"/> -->
</configuration>
```

### Gradle Plugin

Apply the `ftgo.logging-conventions` plugin to automatically include all logging dependencies:

```groovy
plugins {
    id 'ftgo.logging-conventions'
}
```

This provides:
- `logstash-logback-encoder` for JSON output
- `slf4j-api` for logging facade
- `shared:ftgo-logging-lib` for MDC, masking, aspects, and shared Logback config

---

## 9. Best Practices

1. **Use SLF4J parameterized logging** to avoid unnecessary string concatenation:
   ```java
   // Good
   log.info("Order {} created for consumer {}", orderId, consumerId);
   
   // Bad — string is always concatenated even if INFO is disabled
   log.info("Order " + orderId + " created for consumer " + consumerId);
   ```

2. **Include context in log messages** — use MDC fields rather than repeating context in the message:
   ```java
   // Good — userId is already in MDC
   log.info("Order created successfully");
   
   // Unnecessary — userId is already in MDC
   log.info("Order created successfully for user {}", userId);
   ```

3. **Log exceptions with the exception object**, not `e.getMessage()`:
   ```java
   // Good — includes full stack trace
   log.error("Failed to process order {}", orderId, exception);
   
   // Bad — loses stack trace
   log.error("Failed to process order: " + exception.getMessage());
   ```

4. **Use guard clauses for expensive debug logging**:
   ```java
   if (log.isDebugEnabled()) {
       log.debug("Processing items: {}", items.stream()
           .map(Item::toString)
           .collect(Collectors.joining(", ")));
   }
   ```

5. **Do not catch and log then re-throw** — this creates duplicate log entries:
   ```java
   // Bad — logged twice if caller also logs
   try { ... } catch (Exception e) {
       log.error("Error", e);
       throw e;
   }
   
   // Good — log at the boundary where the error is handled
   try { ... } catch (Exception e) {
       throw new OrderProcessingException("Failed to create order", e);
   }
   ```
