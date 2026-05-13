# Centralized Logging Architecture

## Overview

The FTGO platform uses **structured JSON logging** with the **ELK stack**
(Elasticsearch, Logstash, Kibana) for centralized log aggregation. The shared
library `ftgo-logging-lib` provides auto-configuration so that each service only
needs to declare a dependency and set a service name.

## Architecture

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ Order Service│    │Consumer Svc  │    │Restaurant Svc│    │Courier Svc   │
│  JSON logs   │    │  JSON logs   │    │  JSON logs   │    │  JSON logs   │
└──────┬───────┘    └──────┬───────┘    └──────┬───────┘    └──────┬───────┘
       │                   │                   │                   │
       └───────────────────┴───────────────────┴───────────────────┘
                                    │
                            ┌───────┴───────┐
                            │   Logstash    │
                            │  :5000 (TCP)  │
                            │  :5044 (Beats)│
                            └───────┬───────┘
                                    │
                            ┌───────┴───────┐
                            │ Elasticsearch │
                            │    :9200      │
                            └───────┬───────┘
                                    │
                            ┌───────┴───────┐
                            │    Kibana     │
                            │    :5601      │
                            └───────────────┘
```

## Components

| Component | Role | Technology |
|-----------|------|------------|
| `ftgo-logging-lib` | Shared auto-configuration library | Logback + logstash-logback-encoder 7.4 |
| Logstash | Log ingestion and transformation | Elastic Logstash 8.13.4 |
| Elasticsearch | Log storage and full-text search | Elastic Elasticsearch 8.13.4 |
| Kibana | Log visualization and dashboards | Elastic Kibana 8.13.4 |

## ftgo-logging-lib

Shared library providing auto-configured structured JSON logging for all services.

**Location:** `libs/ftgo-logging-lib/`

**What it provides:**
- Auto-configuration via Spring Boot's `AutoConfiguration.imports`
- Structured JSON log output via logstash-logback-encoder
- Correlation ID propagation (`X-Correlation-ID` header → MDC)
- Service context injection (service name, HTTP method, request URI)
- Shared Logback XML configuration for JSON and plain-text appenders
- Integration with distributed tracing (traceId/spanId from ftgo-tracing-lib)

**Integration:** Add to any service's `build.gradle`:
```groovy
implementation project(":ftgo-logging-lib")
```

### Configuration Properties

All properties are under the `ftgo.logging` prefix:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | `true` | Master switch for logging auto-configuration |
| `service-name` | String | `ftgo-service` | Service name injected into every log entry |
| `json-enabled` | boolean | `true` | Enable structured JSON output |
| `correlation-id-enabled` | boolean | `true` | Enable correlation ID filter |
| `logstash.destination` | String | `localhost:5000` | Logstash TCP destination for log shipping |

### Example Service Configuration

```yaml
ftgo:
  logging:
    service-name: order-service
    json-enabled: true
    correlation-id-enabled: true
    logstash:
      destination: logstash:5000
```

### Logback Integration

Include the shared JSON appender in your service's `logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="ftgo/logback-json.xml"/>

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

The `LOGSTASH_TCP` appender ships logs directly to Logstash via TCP. It reads the
destination from `ftgo.logging.logstash.destination` (defaults to `localhost:5044`).
It is defined in the shared config but only active when referenced in a service's
`logback-spring.xml`.

## Structured Log Format

Every log entry is emitted as a single-line JSON object:

```json
{
  "@timestamp": "2024-05-13T10:15:30.123Z",
  "@version": "1",
  "message": "Order created successfully",
  "logger_name": "n.c.f.order.domain.OrderService",
  "thread_name": "http-nio-8080-exec-1",
  "level": "INFO",
  "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "traceId": "64f8e6b2c3a4d5e6",
  "spanId": "1a2b3c4d5e6f7890",
  "serviceName": "order-service",
  "httpMethod": "POST",
  "requestUri": "/orders"
}
```

## MDC Fields

| MDC Key | Source | Description |
|---------|--------|-------------|
| `correlationId` | `X-Correlation-ID` header or auto-generated UUID | Request correlation across services |
| `traceId` | Micrometer Tracing / Brave (via ftgo-tracing-lib) | Distributed trace ID |
| `spanId` | Micrometer Tracing / Brave (via ftgo-tracing-lib) | Current span ID |
| `serviceName` | `ftgo.logging.service-name` property | Logical service name |
| `httpMethod` | HTTP request | GET, POST, PUT, etc. |
| `requestUri` | HTTP request | Request URI path |

## ELK Stack

### Running the Logging Stack

```bash
cd infrastructure/logging
docker-compose -f docker-compose.logging.yml up -d
```

**Access:**

| Component | URL |
|-----------|-----|
| Elasticsearch | http://localhost:9200 |
| Kibana | http://localhost:5601 |
| Logstash Monitoring | http://localhost:9600 |

### Logstash Pipeline

The pipeline (`infrastructure/logging/logstash/pipeline/ftgo-pipeline.conf`):

1. **Input** — Accepts JSON logs via TCP (:5000) and Beats (:5044)
2. **Filter** — Parses JSON, extracts MDC fields, normalizes log levels
3. **Output** — Indexes to Elasticsearch as `ftgo-logs-{service}-{date}`

### Kibana Dashboards

Two pre-configured dashboards:

1. **FTGO — Log Overview**
   - Log volume over time by service
   - Log level distribution (ERROR, WARN, INFO, DEBUG)
   - Recent error logs
   - Top loggers by event count
   - Logs by service breakdown

2. **FTGO — Error Analysis**
   - Error rate over time by service
   - Top error-producing loggers
   - Most common error messages
   - Correlated error traces (with traceId)

Import dashboards after Kibana starts:
```bash
curl -X POST "http://localhost:5601/api/saved_objects/_import" \
  -H "kbn-xsrf: true" \
  --form file=@kibana/dashboards/ftgo-log-overview.ndjson
```

### Index Pattern

Create the index pattern `ftgo-logs-*` in Kibana → Stack Management → Index Patterns.

## Per-Service Setup

### 1. Add Dependency

```groovy
dependencies {
    implementation project(':ftgo-logging-lib')
}
```

### 2. Configure Application Properties

```yaml
ftgo:
  logging:
    service-name: ${SERVICE_NAME:my-service}
    logstash:
      destination: ${LOGSTASH_DESTINATION:logstash:5000}
```

### 3. Add Logback Configuration

Create `src/main/resources/logback-spring.xml` in your service:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="ftgo/logback-json.xml"/>
    <root level="INFO">
        <appender-ref ref="JSON_CONSOLE"/>
    </root>
</configuration>
```

## Relationship to Existing Observability

| Layer | Tool | Purpose |
|-------|------|---------|
| Metrics | `ftgo-metrics-lib` + Prometheus + Grafana | Counters, gauges, histograms |
| Tracing | `ftgo-tracing-lib` + Zipkin | Request flow across services |
| Logging | `ftgo-logging-lib` + ELK Stack | Structured log aggregation and search |

Logs are correlated with traces via the `traceId` and `spanId` MDC keys, which
are automatically populated when `ftgo-tracing-lib` is on the classpath.

## Production Considerations

- Enable Elasticsearch security (`xpack.security.enabled: true`) and TLS
- Configure Index Lifecycle Management (ILM) for log retention policies
- Use dedicated Elasticsearch nodes for production workloads
- Reduce log verbosity: set root level to `WARN`, keep business loggers at `INFO`
- Use Filebeat for file-based log shipping instead of direct TCP
- Set up Elasticsearch snapshots for backup and disaster recovery
- Monitor Elasticsearch cluster health via Prometheus exporter
