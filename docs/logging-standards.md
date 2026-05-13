# FTGO Platform — Logging Standards

This document defines the logging conventions that all FTGO microservices **must** follow
to ensure consistent, searchable, and actionable log output across the platform.

---

## 1. Log Levels

| Level   | When to Use                                                                 |
|---------|-----------------------------------------------------------------------------|
| `ERROR` | Unrecoverable failures that require immediate attention (e.g., data loss, unhandled exceptions, external service hard failures) |
| `WARN`  | Recoverable issues or degraded behaviour (e.g., retries exhausted but fallback used, configuration defaulting) |
| `INFO`  | Significant business events and state changes (e.g., order created, payment processed, courier assigned) |
| `DEBUG` | Detailed technical information for troubleshooting (e.g., method entry/exit, intermediate values, SQL queries) |
| `TRACE` | Very fine-grained detail (e.g., loop iterations, byte-level I/O). Rarely used outside libraries |

### Guidelines

- Default production root level: `WARN`
- FTGO business packages (`net.chrisrichardson.ftgo`): `INFO` in production, `DEBUG` in local
- Framework loggers (`org.springframework`, `org.hibernate`): `WARN` or `ERROR` in production
- **Never** log at `INFO` or above inside tight loops or high-throughput data paths

---

## 2. What to Log

### Always Log

- Service startup and shutdown events
- Incoming request summary (method, URI, correlationId) — handled by `ServiceContextFilter`
- Business domain events (order created, restaurant accepted, courier assigned)
- Authentication/authorization decisions (success/failure — **never** the credentials)
- External service call results (status code, latency)
- Errors and exceptions with full stack traces at `ERROR` level

### Never Log

| Category              | Examples                                                  |
|-----------------------|-----------------------------------------------------------|
| **Credentials**       | Passwords, API keys, tokens, secrets, private keys        |
| **Payment data**      | Full credit card numbers, CVVs, bank account numbers      |
| **PII at high volume**| Email addresses, phone numbers, SSNs in bulk processing   |
| **Request/response bodies** | Full HTTP bodies (may contain PII or payment data)  |
| **Internal IPs/ports**| Production infrastructure topology                        |

The `SensitiveDataMaskingConverter` provides a safety net, but developers must avoid
logging sensitive data in the first place.

---

## 3. Structured Logging Format

All services **must** use structured JSON logging in deployed environments
(docker, k8s). The `ftgo-logging-lib` provides this via
`logstash-logback-encoder`.

### Required Fields

Every JSON log entry contains:

| Field           | Source                          | Example                              |
|-----------------|---------------------------------|--------------------------------------|
| `@timestamp`    | Logback                         | `2024-05-13T10:15:30.123Z`          |
| `level`         | Log statement                   | `INFO`                               |
| `message`       | Log statement                   | `Order created successfully`         |
| `logger_name`   | Logback                         | `n.c.f.order.domain.OrderService`    |
| `thread_name`   | JVM                             | `http-nio-8080-exec-1`              |
| `serviceName`   | `ftgo.logging.service-name`     | `order-service`                      |
| `correlationId` | `X-Correlation-ID` header / UUID| `a1b2c3d4-e5f6-7890-abcd-...`       |

### Optional MDC Fields

| Field          | Source                     | When present                              |
|----------------|----------------------------|-------------------------------------------|
| `traceId`      | ftgo-tracing-lib (Brave)   | When distributed tracing is enabled        |
| `spanId`       | ftgo-tracing-lib (Brave)   | When distributed tracing is enabled        |
| `userId`       | Application code via `LogContext` | After authentication                 |
| `requestId`    | Application code via `LogContext` | Alias for correlationId if set manually |
| `orderId`      | Application code via `LogContext` | During order processing               |
| `restaurantId` | Application code via `LogContext` | During restaurant operations          |

---

## 4. MDC (Mapped Diagnostic Context) Usage

### Standard MDC Keys

Use the constants defined in `LogContext`:

```java
import net.chrisrichardson.ftgo.logging.LogContext;

// Set after authentication
LogContext.putUserId(authenticatedUser.getId().toString());

// Set for domain-specific context
LogContext.putOrderId(order.getId().toString());
```

### Async Context Propagation

When submitting work to thread pools or `@Async` methods, wrap the task:

```java
executor.submit(LogContext.wrap(() -> {
    // MDC context from the parent thread is available here
    processOrder(orderId);
}));
```

### Cleanup

MDC is automatically cleaned by the servlet filters for HTTP requests.
For non-HTTP contexts (message consumers, scheduled tasks), call
`LogContext.clear()` in a `finally` block.

---

## 5. Sensitive Data Masking

The `SensitiveDataMaskingConverter` is registered in `logback-spring.xml` and
automatically masks the following patterns in log messages:

| Pattern              | Example Input                              | Masked Output              |
|----------------------|--------------------------------------------|----------------------------|
| Credit card numbers  | `4111-1111-1111-1111`                      | `4111-****-****-1111`      |
| Passwords            | `password=secret123`                       | `password=****`            |
| Bearer tokens        | `Bearer eyJhbGciOi...`                     | `Bearer ****`              |
| Basic auth           | `Authorization: Basic dXNlcjpwYXNz`       | `Authorization: Basic ****`|
| API keys             | `api_key=abc123secret`                     | `api_key=****`             |
| SSNs (hyphenated)    | `123-45-6789`                              | `***-**-6789`              |
| Tokens/secrets       | `token=abc123`                             | `token=****`               |

> **Important:** Masking is a safety net, not a substitute for careful logging.
> Do not intentionally log sensitive data and rely on masking.

---

## 6. Per-Environment Configuration

### Profiles

| Profile  | Output Format     | Root Level | FTGO Level | Logstash | Use Case       |
|----------|-------------------|------------|------------|----------|----------------|
| `local`  | Human-readable    | `INFO`     | `DEBUG`    | No       | Developer laptop |
| `docker` | JSON (console)    | `INFO`     | `INFO`     | No       | Docker Compose  |
| `k8s`    | JSON (console+TCP)| `WARN`     | `INFO`     | Yes      | Production K8s  |

### Overriding Log Levels at Runtime

Use Spring Boot property overrides without redeployment:

```bash
# Via environment variable
LOGGING_LEVEL_NET_CHRISRICHARDSON_FTGO=DEBUG

# Via JVM system property
-Dlogging.level.net.chrisrichardson.ftgo=DEBUG

# Via Spring Boot Actuator (if enabled)
POST /actuator/loggers/net.chrisrichardson.ftgo
{"configuredLevel": "DEBUG"}
```

---

## 7. Logging Aspect (`@Logged`)

Use the `@Logged` annotation for automatic entry/exit logging:

```java
import net.chrisrichardson.ftgo.logging.Logged;

@Service
public class OrderService {

    @Logged
    public Order createOrder(CreateOrderRequest request) {
        // Method entry logged at DEBUG with args
        // Method exit logged at DEBUG with elapsed time
        // Exceptions logged at WARN
        return ...;
    }
}
```

Place `@Logged` on a class to log all public methods:

```java
@Logged
@Service
public class PaymentService {
    // All public methods are automatically logged
}
```

### Sensitive Parameters

`@Logged` logs method arguments at `DEBUG` level via `Arrays.toString()`. **Do not**
apply `@Logged` to methods that accept sensitive parameters (passwords, tokens, PII).
If you need entry/exit logging on such methods, log manually and omit the sensitive args.

### Performance

- Entry/exit logging is at `DEBUG` level — zero overhead in production (`INFO`+)
- Exceptions are logged at `WARN` regardless of configured level
- Use on service-layer methods; avoid on repository or utility methods

---

## 8. Integration with Existing Libraries

| Library            | Integration Point                                      |
|--------------------|--------------------------------------------------------|
| `ftgo-tracing-lib` | `traceId`/`spanId` added to MDC automatically by Brave |
| `ftgo-metrics-lib` | Log errors → increment error counters                  |
| `ftgo-security-lib`| Set `userId` in MDC after authentication               |

---

## 9. Code Examples

### Service Method with Structured Logging

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.chrisrichardson.ftgo.logging.LogContext;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public Order createOrder(long consumerId, long restaurantId, List<LineItem> items) {
        LogContext.putUserId(String.valueOf(consumerId));
        LogContext.putRestaurantId(String.valueOf(restaurantId));
        try {
            log.info("Creating order for consumer");
            Order order = orderRepository.save(new Order(consumerId, restaurantId, items));
            LogContext.putOrderId(order.getId().toString());
            log.info("Order created successfully");
            return order;
        } catch (Exception e) {
            log.error("Failed to create order", e);
            throw e;
        }
    }
}
```

### Anti-Patterns

```java
// BAD: logging sensitive data
log.info("User {} logged in with password {}", user, password);

// BAD: string concatenation instead of parameterized logging
log.debug("Processing order " + orderId + " for user " + userId);

// BAD: logging at INFO in a loop
for (Item item : items) {
    log.info("Processing item {}", item);  // Use DEBUG
}

// GOOD: parameterized logging
log.info("Order {} created for consumer {}", orderId, consumerId);

// GOOD: guarded DEBUG
if (log.isDebugEnabled()) {
    log.debug("Order details: {}", order.toDetailString());
}
```
