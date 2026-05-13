# Distributed Tracing Architecture

## Overview

The FTGO platform uses **Micrometer Tracing** with **Brave** as the tracer
implementation and **Zipkin** as the trace collection and visualization backend.
The shared library `ftgo-tracing-lib` provides auto-configuration so that each
service only needs to declare a dependency and set a service name.

## Components

| Component | Role | Technology |
|-----------|------|------------|
| `ftgo-tracing-lib` | Shared auto-configuration library | Micrometer Tracing 1.2.x, Brave |
| Zipkin Server | Trace collection, storage, and UI | openzipkin/zipkin:3.4 |
| W3C Trace Context | Default propagation format | `traceparent` / `tracestate` headers |
| B3 Propagation | Legacy fallback format | `X-B3-TraceId` / `X-B3-SpanId` headers |

## How It Works

### Trace Context Propagation

```
Consumer ──HTTP──▶ Order Service ──HTTP──▶ Restaurant Service
         traceparent:              traceparent:
         00-<traceId>-<spanId>-01  00-<traceId>-<spanId>-01
```

Every outbound HTTP request carries a `traceparent` header (W3C Trace Context
format by default). The receiving service extracts the trace and span IDs from
the header, creates a child span, and forwards the context to downstream calls.

### Span Reporting

Each service sends completed spans to the Zipkin collector asynchronously via
HTTP POST to `/api/v2/spans`. The `AsyncReporter` batches spans to minimize
network overhead.

```
Service ──async POST──▶ Zipkin :9411/api/v2/spans ──▶ Storage
```

## Library Architecture

```
ftgo-tracing-lib
├── FtgoTracingAutoConfiguration    # Spring Boot auto-configuration
│   ├── URLConnectionSender         # HTTP sender to Zipkin
│   ├── AsyncReporter               # Batched span reporter
│   ├── ZipkinSpanHandler           # Brave → Zipkin bridge
│   ├── Tracing (Brave)             # Core tracing engine
│   ├── BraveTracer                 # Micrometer ↔ Brave bridge
│   ├── TracingInterceptor          # HTTP request/response tagging
│   └── TracingWebMvcConfigurer     # Registers the interceptor
├── FtgoTracingProperties           # Configuration properties
├── W3CTraceContextPropagation      # W3C traceparent format
├── @Traced annotation              # Custom span AOP annotation
└── TracedAspect                    # AOP around-advice for @Traced
```

## Configuration Reference

All properties are under the `ftgo.tracing` prefix:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | `true` | Master switch for tracing |
| `service-name` | String | `ftgo-service` | Service name in traces |
| `sampling-probability` | float | `1.0` | Fraction of requests to trace (0.0–1.0) |
| `propagation.type` | enum | `W3C` | Context propagation format: `W3C` or `B3` |
| `zipkin.endpoint` | String | `http://localhost:9411/api/v2/spans` | Zipkin collector URL |

### Example Service Configuration

```yaml
ftgo:
  tracing:
    service-name: order-service
    sampling-probability: 1.0
    propagation:
      type: W3C
    zipkin:
      endpoint: http://zipkin:9411/api/v2/spans
```

## Custom Spans with @Traced

The `@Traced` annotation creates a new span around any method:

```java
@Service
public class OrderService {

    @Traced("orderService.createOrder")
    public Order createOrder(CreateOrderRequest request) {
        // Business logic — automatically wrapped in a span
    }

    @Traced  // defaults to "OrderService.approveOrder"
    public void approveOrder(long orderId) {
        // ...
    }
}
```

The aspect records:
- Span name (from annotation value or `ClassName.methodName`)
- `class` and `method` tags
- Exception details on failure

## Per-Service Setup

### 1. Add Dependency

```groovy
// build.gradle
dependencies {
    implementation project(':ftgo-tracing-lib')
}
```

### 2. Configure Application Properties

```yaml
ftgo:
  tracing:
    service-name: ${SERVICE_NAME:my-service}
    zipkin:
      endpoint: ${ZIPKIN_ENDPOINT:http://zipkin:9411/api/v2/spans}
```

### 3. (Optional) Add Custom Spans

Annotate business methods with `@Traced`.

## Infrastructure

### Development

```bash
cd infrastructure/tracing
docker-compose -f docker-compose-zipkin.yml up -d
# Zipkin UI: http://localhost:9411
```

### Production Considerations

- Switch Zipkin storage to Elasticsearch for durability
- Reduce `sampling-probability` to `0.1`–`0.01` under high load
- Use the Zipkin dependency graph to visualize service topology
- Set up alerts on trace error rates via Zipkin API

## Relationship to Existing Observability

| Layer | Tool | Purpose |
|-------|------|---------|
| Metrics | `ftgo-metrics-lib` + Prometheus + Grafana | Counters, gauges, histograms |
| Tracing | `ftgo-tracing-lib` + Zipkin | Request flow across services |
| Logging | SLF4J + Logback | Structured application logs |

Trace IDs are available via Micrometer's MDC integration, allowing logs to be
correlated with traces by including `traceId` and `spanId` in log patterns:

```xml
<pattern>%d{ISO8601} [%thread] [traceId=%X{traceId} spanId=%X{spanId}] %-5level %logger - %msg%n</pattern>
```
