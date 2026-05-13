# ftgo-tracing-lib

Shared distributed tracing library for FTGO platform microservices.

## Features

- **Auto-configuration** — Drop-in Spring Boot starter that configures Micrometer Tracing with Brave and Zipkin export.
- **W3C Trace Context** propagation (default) with B3 fallback for legacy services.
- **`@Traced` annotation** — Create custom spans for business operations via AOP.
- **HTTP interceptor** — Automatically tags incoming requests with method, URL, and status.

## Quick Start

Add the dependency to your service's `build.gradle`:

```groovy
implementation project(':ftgo-tracing-lib')
```

Configure in `application.yml`:

```yaml
ftgo:
  tracing:
    enabled: true
    service-name: order-service
    sampling-probability: 1.0
    propagation:
      type: W3C          # or B3
    zipkin:
      endpoint: http://zipkin:9411/api/v2/spans
```

## Custom Spans

Annotate business methods:

```java
@Traced("orderService.createOrder")
public Order createOrder(CreateOrderRequest request) {
    // ...
}
```

## Configuration Properties

| Property | Default | Description |
|---|---|---|
| `ftgo.tracing.enabled` | `true` | Enable/disable tracing |
| `ftgo.tracing.service-name` | `ftgo-service` | Logical service name in traces |
| `ftgo.tracing.sampling-probability` | `1.0` | Sampling rate (0.0 – 1.0) |
| `ftgo.tracing.propagation.type` | `W3C` | Propagation format (`W3C` or `B3`) |
| `ftgo.tracing.zipkin.endpoint` | `http://localhost:9411/api/v2/spans` | Zipkin collector URL |
