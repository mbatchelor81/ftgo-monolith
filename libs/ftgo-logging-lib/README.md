# ftgo-logging-lib

Shared structured logging library for FTGO platform microservices.

## Features

- **Auto-configuration** — Drop-in Spring Boot starter that configures structured JSON logging via logstash-logback-encoder.
- **Correlation ID** — Automatically generates or propagates `X-Correlation-ID` header and adds it to MDC for log correlation.
- **Service context** — Injects service name, HTTP method, and request URI into MDC for every request.
- **Trace context integration** — Includes `traceId` and `spanId` from Micrometer Tracing (ftgo-tracing-lib) in structured logs.
- **Shared Logback config** — Provides reusable JSON and plain-text appender configurations with per-environment profiles.
- **Sensitive data masking** — Automatically masks credit cards, passwords, tokens, and other sensitive patterns in log output.
- **MDC utilities** — `LogContext` class for consistent MDC field management and async context propagation.
- **Logging aspect** — `@Logged` annotation for automatic method entry/exit logging via AOP.

## Quick Start

Add the dependency to your service's `build.gradle`:

```groovy
implementation project(':ftgo-logging-lib')
```

Configure in `application.yml`:

```yaml
ftgo:
  logging:
    enabled: true
    service-name: order-service
    json-enabled: true
    correlation-id-enabled: true
    logstash:
      destination: logstash:5000
```

## Logback Configuration

Include the shared `logback-spring.xml` in your service, which provides
profile-based appender selection (local → human-readable, docker → JSON,
k8s → JSON + Logstash), sensitive data masking, and per-environment log levels:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="ftgo/logback-spring.xml"/>
</configuration>
```

Or use the individual appender fragments for more control:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="ftgo/logback-json.xml"/>
    <include resource="ftgo/logback-logstash.xml"/>

    <springProfile name="default,docker,k8s">
        <root level="INFO">
            <appender-ref ref="JSON_CONSOLE"/>
            <appender-ref ref="LOGSTASH_TCP"/>
        </root>
    </springProfile>

    <springProfile name="local">
        <root level="INFO">
            <appender-ref ref="PLAIN_CONSOLE"/>
        </root>
    </springProfile>
</configuration>
```

## Sensitive Data Masking

The `SensitiveDataMaskingConverter` automatically masks sensitive patterns in
log messages when using the shared `logback-spring.xml`:

| Pattern              | Example Input                     | Masked Output              |
|----------------------|-----------------------------------|----------------------------|
| Credit card numbers  | `4111-1111-1111-1111`             | `4111-****-****-1111`      |
| Passwords            | `password=secret123`              | `password=****`            |
| Bearer tokens        | `Bearer eyJhbGciOi...`            | `Bearer ****`              |
| API keys             | `api_key=abc123`                  | `api_key=****`             |
| SSNs                 | `123-45-6789`                     | `***-**-6789`              |

## MDC Utilities (`LogContext`)

```java
import net.chrisrichardson.ftgo.logging.LogContext;

// Set context fields
LogContext.putUserId("user-42");
LogContext.putOrderId("order-99");

// Propagate to async threads
executor.submit(LogContext.wrap(() -> {
    // MDC context from parent thread is available
    processOrder(orderId);
}));

// Capture/restore snapshots
Map<String, String> snapshot = LogContext.snapshot();
LogContext.restore(snapshot);
```

## Logging Aspect (`@Logged`)

```java
import net.chrisrichardson.ftgo.logging.Logged;

@Service
public class OrderService {
    @Logged
    public Order createOrder(CreateOrderRequest request) {
        // Entry logged at DEBUG with args, exit with elapsed time
        return ...;
    }
}
```

## MDC Fields

The following MDC keys are available in all log entries:

| MDC Key | Source | Description |
|---------|--------|-------------|
| `correlationId` | `X-Correlation-ID` header or auto-generated UUID | Request correlation identifier |
| `traceId` | Micrometer Tracing / Brave (via ftgo-tracing-lib) | Distributed trace ID |
| `spanId` | Micrometer Tracing / Brave (via ftgo-tracing-lib) | Current span ID |
| `serviceName` | `ftgo.logging.service-name` property | Logical service name |
| `httpMethod` | HTTP request | GET, POST, PUT, etc. |
| `requestUri` | HTTP request | Request URI path |
| `userId` | Application code via `LogContext` | Authenticated user ID |
| `requestId` | Application code via `LogContext` | Request identifier (alias) |
| `orderId` | Application code via `LogContext` | Order identifier |
| `restaurantId` | Application code via `LogContext` | Restaurant identifier |

## Per-Environment Configuration

| Profile  | Output Format     | Root Level | FTGO Level | Logstash |
|----------|-------------------|------------|------------|----------|
| `local`  | Human-readable    | `INFO`     | `DEBUG`    | No       |
| `docker` | JSON (console)    | `INFO`     | `INFO`     | No       |
| `k8s`    | JSON (console+TCP)| `WARN`     | `INFO`     | Yes      |

Reference property files are provided in `src/main/resources/ftgo/`:
- `logging-defaults-local.properties`
- `logging-defaults-docker.properties`
- `logging-defaults-k8s.properties`

## Configuration Properties

| Property | Default | Description |
|---|---|---|
| `ftgo.logging.enabled` | `true` | Enable/disable logging auto-configuration |
| `ftgo.logging.service-name` | `ftgo-service` | Logical service name in logs |
| `ftgo.logging.json-enabled` | `true` | Enable structured JSON output |
| `ftgo.logging.correlation-id-enabled` | `true` | Enable correlation ID filter |
| `ftgo.logging.logstash.destination` | `localhost:5000` | Logstash TCP destination for log shipping |
