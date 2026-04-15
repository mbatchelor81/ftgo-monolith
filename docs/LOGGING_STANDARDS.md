# FTGO Logging Standards

This document defines the logging conventions that **all FTGO microservices** must follow.
Consistent, structured, and secure logging is essential for debugging, auditing, and
operating the platform at scale.

> **Shared implementation** — The `ftgo-observability-lib` module provides a shared
> `logback-spring.xml`, MDC filters, sensitive-data masking, and logging utilities.
> Services that depend on `ftgo-observability-lib` inherit these defaults automatically.

---

## 1. Log Levels

Use the correct level so that operators can filter noise without losing signal.

| Level   | When to Use                                                                 | Examples                                                        |
|---------|-----------------------------------------------------------------------------|-----------------------------------------------------------------|
| `ERROR` | Unrecoverable failure requiring immediate attention                         | Database connection lost, payment gateway timeout, unhandled exception |
| `WARN`  | Unexpected condition that the system can recover from                        | Retry succeeded after transient failure, deprecated API called, fallback activated |
| `INFO`  | Significant business or lifecycle event                                     | Order created, restaurant accepted order, service started/stopped |
| `DEBUG` | Detailed technical context useful during development or troubleshooting      | SQL parameters, HTTP request/response details, cache hit/miss   |
| `TRACE` | Very fine-grained diagnostic output (rarely enabled outside local dev)       | Loop iterations, field-level state changes, serialization steps |

### Rules of Thumb

- **Production** should run at `INFO` for application code and `ERROR` for framework code.
- Never log at `ERROR` for conditions the caller can handle (use `WARN` instead).
- Avoid logging inside tight loops at `INFO` or above — use `DEBUG` or `TRACE`.

---

## 2. What to Log

### Always Log

| Event                           | Level   | Details to Include                              |
|---------------------------------|---------|-------------------------------------------------|
| Service startup / shutdown      | `INFO`  | Service name, version, active profiles          |
| Inbound request received        | `DEBUG` | HTTP method, URI, correlation ID                |
| Business event completed        | `INFO`  | Entity type, entity ID, action (created/updated/cancelled) |
| External service call           | `DEBUG` | Target service, endpoint, latency               |
| External service call failure   | `WARN`  | Target service, endpoint, HTTP status, error message |
| Unhandled exception             | `ERROR` | Exception class, message, stack trace           |
| Authentication / authorization  | `INFO`  | userId, action, outcome (success/failure)       |
| Retry / circuit-breaker events  | `WARN`  | Operation name, attempt number, reason          |

### Never Log

| Data                            | Reason                                                     |
|---------------------------------|------------------------------------------------------------|
| Passwords / secrets             | Security — credentials must never appear in logs           |
| Credit card numbers (full)      | PCI-DSS compliance                                         |
| Authentication tokens / JWTs    | Leaked tokens allow impersonation                          |
| PII beyond what is necessary    | GDPR / privacy — log user IDs, not names or emails         |
| Full request / response bodies  | In production these can contain sensitive data and are noisy |
| Health-check requests           | High volume, low value — creates log noise                 |

> **Masking** — The shared `SensitiveDataMaskingConverter` in `ftgo-observability-lib`
> automatically redacts credit card numbers, passwords, bearer tokens, and basic-auth
> headers that accidentally appear in log messages. This is a safety net, **not a
> substitute for writing clean log statements**.

---

## 3. MDC (Mapped Diagnostic Context) Fields

Every log entry automatically includes the following MDC fields when the request
passes through the FTGO servlet filters:

| MDC Key           | Source                          | Description                                    |
|-------------------|---------------------------------|------------------------------------------------|
| `correlationId`   | `X-Correlation-ID` header / UUID | Links all log entries for a single request      |
| `requestId`       | `X-Request-ID` header / UUID    | Unique identifier per HTTP request              |
| `userId`          | `X-User-ID` header              | Authenticated user making the request           |
| `service`         | `spring.application.name`       | Name of the service emitting the log            |
| `requestMethod`   | HTTP method                     | GET, POST, PUT, DELETE, etc.                    |
| `requestUri`      | HTTP request URI                | The path of the incoming request                |
| `traceId`         | Micrometer Tracing (Brave)      | Distributed trace identifier                    |
| `spanId`          | Micrometer Tracing (Brave)      | Current span identifier                         |

### Setting MDC Programmatically

Use `LogContext` for non-HTTP contexts (message consumers, scheduled tasks, etc.):

```java
import com.ftgo.observability.logging.LogContext;

// Set context for async processing
LogContext.setUserId("user-123");
LogContext.setRequestId("batch-run-456");
try {
    // ... business logic — all log statements include the MDC fields
} finally {
    LogContext.clear();
}
```

---

## 4. Structured Logging Format

### Local Development (default profile)

Human-readable, single-line format with trace context:

```
2025-01-15 10:30:45.123 [http-nio-8080-exec-1] [abc123,def456] [corr-789] INFO  c.f.o.OrderService - Order 42 created for consumer 7
```

Pattern: `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId:-},%X{spanId:-}] [%X{correlationId:-}] %-5level %logger{36} - %msg%n`

### Deployed Environments (docker / kubernetes profiles)

JSON format via `logstash-logback-encoder`, consumed by the EFK stack:

```json
{
  "@timestamp": "2025-01-15T10:30:45.123+0000",
  "level": "INFO",
  "logger_name": "c.f.o.OrderService",
  "message": "Order 42 created for consumer 7",
  "thread_name": "http-nio-8080-exec-1",
  "service": "ftgo-order-service",
  "traceId": "abc123",
  "spanId": "def456",
  "correlationId": "corr-789",
  "requestId": "req-012",
  "userId": "user-7",
  "requestMethod": "POST",
  "requestUri": "/api/orders"
}
```

---

## 5. Per-Environment Log Levels

| Profile               | Application Code (`com.ftgo`) | Spring Framework        | Hibernate SQL           |
|-----------------------|-------------------------------|-------------------------|-------------------------|
| `local` (default)     | `DEBUG`                       | `INFO`                  | `DEBUG`                 |
| `dev` / `staging`     | `INFO`                        | `WARN`                  | `WARN`                  |
| `docker` / `kubernetes` (production) | `INFO`          | `ERROR`                 | `ERROR`                 |

These defaults are configured in the shared `logback-spring.xml`. Services may override
specific loggers in their own `application.yml` if needed.

---

## 6. Logging Aspect (Method Entry/Exit)

The `LoggingAspect` in `ftgo-observability-lib` provides automatic method entry/exit
logging for service-layer methods. It is **opt-in** per service via the
`ftgo.logging.aspect.enabled` property.

### What It Logs

- **Entry** (`DEBUG`): method name and arguments
- **Exit** (`DEBUG`): method name and execution time in milliseconds
- **Exception** (`DEBUG`): method name, exception class, and message (error-level logging is left to the global exception handler)

### Configuration

```yaml
ftgo:
  logging:
    aspect:
      enabled: true                        # default: true
      base-packages: com.ftgo              # default: com.ftgo
```

---

## 7. Async Logging

In deployed environments (`docker` / `kubernetes` profiles), all JSON log output is
wrapped in a Logback `AsyncAppender`:

| Setting                | Value   | Rationale                                       |
|------------------------|---------|-------------------------------------------------|
| `queueSize`            | `1024`  | Buffer 1024 log events before blocking           |
| `discardingThreshold`  | `0`     | Never discard — all levels are important         |
| `neverBlock`           | `true`  | Drop rather than block request threads           |
| `includeCallerData`    | `false` | Avoids expensive stack-trace capture per event   |

---

## 8. File Rotation (Local Development)

A file appender is available for local development under `logs/ftgo-service.log`:

| Setting            | Value       |
|--------------------|-------------|
| Max file size      | `50 MB`     |
| Max history        | `7 days`    |
| Total size cap     | `500 MB`    |

This appender is only active when running **without** the `docker` or `kubernetes` profiles.

---

## 9. Quick Reference: Logging Do's and Don'ts

### Do

- Use SLF4J parameterized logging: `log.info("Order {} created", orderId)`
- Include entity IDs and action verbs in messages
- Use MDC for cross-cutting context (user, request, correlation)
- Let the shared `logback-spring.xml` handle formatting

### Don't

- Don't use string concatenation: `log.info("Order " + orderId + " created")` — this
  evaluates the string even when the level is disabled
- Don't log and throw: pick one or the other to avoid duplicate noise
- Don't catch-and-log-and-rethrow without adding context
- Don't create custom Logback configurations in individual services unless absolutely
  necessary — extend the shared configuration instead
