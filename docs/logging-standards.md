# FTGO Logging Standards

## Log Levels

| Level | When to Use | Examples |
|-------|------------|---------|
| **ERROR** | Unrecoverable failures requiring immediate attention | Database connection lost, external service unavailable after retries, data corruption detected |
| **WARN** | Recoverable issues that should be investigated | Retry succeeded after failure, deprecated API called, approaching resource limits |
| **INFO** | Key business events and operational milestones | Order created, payment processed, service started, configuration loaded |
| **DEBUG** | Detailed diagnostic information for troubleshooting | Method entry/exit, query parameters, intermediate calculation results |
| **TRACE** | Very fine-grained diagnostic output | Full request/response bodies (dev only), loop iterations, cache hit/miss details |

## What to Log

### Always Log
- Service startup and shutdown events
- Incoming HTTP requests (method, path, status, duration) — handled by `RequestLoggingMdcFilter`
- External service calls (target, duration, success/failure)
- Business domain events (order state transitions, payment events)
- Authentication events (login success/failure, token refresh)
- Error conditions with full context (exception type, message, relevant IDs)
- Configuration changes and feature flag evaluations

### Never Log
- **Passwords, tokens, or API keys** — use `[MASKED]` placeholder
- **Credit card numbers** — mask all but last 4 digits: `****-****-****-1234`
- **Social Security Numbers or national IDs** — never log, even masked
- **Full request/response bodies in production** — use DEBUG/TRACE level only
- **Personal Identifiable Information (PII)** — emails, phone numbers, addresses
  - If logging is unavoidable, use the `PiiMaskingConverter` to auto-mask
- **Health check endpoint traffic** — excluded by `RequestLoggingMdcFilter`

## MDC (Mapped Diagnostic Context) Fields

Every log entry automatically includes these MDC fields via shared filters:

| Field | Source | Description |
|-------|--------|-------------|
| `correlationId` | `X-Correlation-Id` header or auto-generated UUID | Ties all logs for a single user request across services |
| `traceId` | Micrometer Tracing / Brave | Distributed trace identifier |
| `spanId` | Micrometer Tracing / Brave | Current span identifier |
| `httpMethod` | `RequestLoggingMdcFilter` | HTTP method (GET, POST, etc.) |
| `requestUri` | `RequestLoggingMdcFilter` | Request URI path |
| `userId` | `LogContext.setUserId()` | Authenticated user identifier (set in security filter) |
| `serviceName` | `spring.application.name` | Name of the service emitting the log |

### Setting Custom MDC Fields

```java
import com.ftgo.common.logging.context.LogContext;

// In a security filter or interceptor
LogContext.setUserId(authenticatedUser.getId());

// In business logic — add temporary context
LogContext.put("orderId", orderId.toString());
try {
    // ... business logic — all logs will include orderId
} finally {
    LogContext.remove("orderId");
}
```

## Log Format

### Deployed Environments (staging, prod)

JSON structured logging via `LogstashEncoder`:
```json
{
  "@timestamp": "2026-03-19T12:00:00.000Z",
  "level": "INFO",
  "logger_name": "c.f.o.d.OrderService",
  "thread_name": "http-nio-8080-exec-1",
  "message": "Order created successfully",
  "correlationId": "abc-123-def",
  "traceId": "64bit-trace-id",
  "spanId": "64bit-span-id",
  "userId": "user-42",
  "service": "ftgo-order-service"
}
```

### Local Development (dev, local, default)

Human-readable console format:
```
2026-03-19 12:00:00.000 INFO  [http-nio-8080-exec-1] c.f.o.d.OrderService : Order created successfully
```

## Per-Environment Log Level Configuration

### Local Development
```yaml
logging:
  level:
    com.ftgo: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql: TRACE
```

### Dev / Staging
```yaml
logging:
  level:
    com.ftgo: INFO
    org.springframework.web: INFO
    org.hibernate.SQL: INFO
    org.springframework: WARN
```

### Production
```yaml
logging:
  level:
    com.ftgo: INFO
    org.springframework: WARN
    org.hibernate: WARN
    org.apache: WARN
```

## Sensitive Data Masking

The `PiiMaskingConverter` automatically masks sensitive patterns in log messages:

| Pattern | Example Input | Masked Output |
|---------|--------------|---------------|
| Credit card numbers | `4111-1111-1111-1234` | `****-****-****-1234` |
| Email addresses | `user@example.com` | `u***@example.com` |
| Bearer tokens | `Bearer eyJhbGciOi...` | `Bearer [MASKED]` |
| Password fields | `"password":"secret123"` | `"password":"[MASKED]"` |

To enable masking, use the `%maskedMessage` conversion word in your Logback config instead of `%message`.

## Async Logging

Production environments use async appenders to prevent logging from blocking request threads:

- Queue capacity: 1024 events
- Discard threshold: 0 (never discard)
- Never block: true (drop logs rather than block threads under extreme load)

This is pre-configured in `ftgo-logback-defaults.xml`.

## Logging Best Practices

1. **Use parameterized logging** — avoid string concatenation:
   ```java
   // Good
   log.info("Order {} created for consumer {}", orderId, consumerId);
   
   // Bad — evaluates even if INFO is disabled
   log.info("Order " + orderId + " created for consumer " + consumerId);
   ```

2. **Include context in error logs**:
   ```java
   // Good — includes exception and context
   log.error("Failed to process order orderId={} consumerId={}", orderId, consumerId, exception);
   
   // Bad — no context
   log.error("Error processing order");
   ```

3. **Use appropriate log levels** — DEBUG for diagnostics, INFO for events, ERROR for failures.

4. **Don't log and throw** — choose one:
   ```java
   // Bad — logs twice (here and in the exception handler)
   log.error("Order not found", e);
   throw new OrderNotFoundException(orderId);
   
   // Good — let the global handler log it
   throw new OrderNotFoundException(orderId);
   ```

5. **Log at service boundaries** — entry/exit of REST controllers and external service calls.
