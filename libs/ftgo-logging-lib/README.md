# ftgo-logging-lib

Shared structured logging library for FTGO platform microservices.

## Features

- **Auto-configuration** — Drop-in Spring Boot starter that configures structured JSON logging via logstash-logback-encoder.
- **Correlation ID** — Automatically generates or propagates `X-Correlation-ID` header and adds it to MDC for log correlation.
- **Service context** — Injects service name, HTTP method, and request URI into MDC for every request.
- **Trace context integration** — Includes `traceId` and `spanId` from Micrometer Tracing (ftgo-tracing-lib) in structured logs.
- **Shared Logback config** — Provides reusable JSON and plain-text appender configurations.

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

Include the shared JSON appender in your service's `logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="ftgo/logback-json.xml"/>

    <springProfile name="default,docker,k8s">
        <root level="INFO">
            <appender-ref ref="JSON_CONSOLE"/>
        </root>
    </springProfile>

    <springProfile name="local">
        <root level="INFO">
            <appender-ref ref="PLAIN_CONSOLE"/>
        </root>
    </springProfile>
</configuration>
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

## Configuration Properties

| Property | Default | Description |
|---|---|---|
| `ftgo.logging.enabled` | `true` | Enable/disable logging auto-configuration |
| `ftgo.logging.service-name` | `ftgo-service` | Logical service name in logs |
| `ftgo.logging.json-enabled` | `true` | Enable structured JSON output |
| `ftgo.logging.correlation-id-enabled` | `true` | Enable correlation ID filter |
| `ftgo.logging.logstash.destination` | `localhost:5000` | Logstash TCP destination for log shipping |
