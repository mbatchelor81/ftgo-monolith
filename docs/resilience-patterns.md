# Resilience Patterns

## Overview

The FTGO platform uses [Resilience4j](https://resilience4j.readme.io/) to implement
fault-tolerance patterns for inter-service communication. The shared library
`ftgo-resilience-lib` provides auto-configured circuit breakers, retries, and bulkheads
that any FTGO microservice can adopt by adding a single dependency.

## Library: `ftgo-resilience-lib`

### Quick Start

Add the dependency to your service's `build.gradle`:

```groovy
dependencies {
    implementation project(':ftgo-resilience-lib')
}
```

The auto-configuration activates automatically when Resilience4j is on the classpath.
No additional annotations or imports are required.

### Disabling

```yaml
ftgo:
  resilience:
    enabled: false
```

---

## Resilience Patterns

### Circuit Breaker

Prevents cascading failures by short-circuiting calls to an unhealthy downstream
service. When the failure rate exceeds a threshold, the circuit opens and fast-fails
for a configured wait period before allowing probe requests through.

**State Machine:**

```
  CLOSED ──(failure rate ≥ threshold)──▸ OPEN
    ▴                                      │
    │                              (wait duration)
    │                                      ▾
    └──────(success in probes)──── HALF_OPEN
```

**Default Configuration:**

| Parameter | Default | Description |
|-----------|---------|-------------|
| `failure-rate-threshold` | 50% | Percentage of failures to trip the circuit |
| `sliding-window-size` | 10 | Number of calls in the sliding window |
| `minimum-number-of-calls` | 5 | Minimum calls before evaluating failure rate |
| `wait-duration-in-open-state-millis` | 30 000 ms | Time the circuit stays open |
| `permitted-number-of-calls-in-half-open-state` | 3 | Probe calls allowed in half-open |

**Customization via `application.yml`:**

```yaml
ftgo:
  resilience:
    circuit-breaker:
      failure-rate-threshold: 60
      sliding-window-size: 20
      wait-duration-in-open-state-millis: 60000
```

**Pre-registered instances:** `orderService`, `consumerService`, `restaurantService`,
`courierService`.

---

### Retry

Automatically retries transient failures (network glitches, temporary unavailability)
with configurable back-off.

**Default Configuration:**

| Parameter | Default | Description |
|-----------|---------|-------------|
| `max-attempts` | 3 | Total attempts (initial + retries) |
| `wait-duration-millis` | 1 000 ms | Base wait between retries |
| `multiplier` | 2.0 | Exponential back-off multiplier |

`IllegalArgumentException` is excluded from retries by default (client errors should
not be retried).

**Customization:**

```yaml
ftgo:
  resilience:
    retry:
      max-attempts: 5
      wait-duration-millis: 2000
```

---

### Bulkhead

Limits concurrent access to downstream services, preventing a single slow service
from exhausting the caller's thread pool.

**Default Configuration:**

| Parameter | Default | Description |
|-----------|---------|-------------|
| `max-concurrent-calls` | 25 | Maximum simultaneous calls |
| `max-wait-duration-millis` | 0 | Max time to wait for a permit (0 = fail fast) |

**Customization:**

```yaml
ftgo:
  resilience:
    bulkhead:
      max-concurrent-calls: 50
      max-wait-duration-millis: 500
```

---

## Health Indicators

The library registers per-service health indicators that expose circuit breaker state
through Spring Boot Actuator at `/actuator/health`.

Each indicator reports:

| Detail | Description |
|--------|-------------|
| `service` | Service name |
| `circuitBreaker.state` | CLOSED, OPEN, or HALF_OPEN |
| `circuitBreaker.failureRate` | Current failure rate percentage |
| `circuitBreaker.bufferedCalls` | Calls in the sliding window |
| `circuitBreaker.failedCalls` | Failed calls in the window |
| `circuitBreaker.successfulCalls` | Successful calls in the window |

When a circuit breaker is OPEN or FORCED_OPEN, the health indicator reports `DOWN`.
This affects the aggregate `/actuator/health` endpoint (used by Consul health checks).

To also affect Kubernetes readiness probes (`/actuator/health/readiness`), consuming
services must add the indicators to the readiness group:

```yaml
management:
  endpoint:
    health:
      group:
        readiness:
          include: readinessState, orderService, consumerService, restaurantService, courierService
```

**Disabling individual indicators:**

```yaml
ftgo:
  resilience:
    health:
      order:
        enabled: false
```

---

## Integration with Observability

### Metrics (ftgo-metrics-lib)

Resilience4j publishes Micrometer metrics automatically when `resilience4j-micrometer`
is on the classpath. Key metrics:

- `resilience4j_circuitbreaker_state` — current circuit state
- `resilience4j_circuitbreaker_calls_seconds` — call duration by outcome
- `resilience4j_retry_calls_total` — retry attempts by kind
- `resilience4j_bulkhead_available_concurrent_calls` — available permits

### Tracing (ftgo-tracing-lib)

Circuit breaker state transitions and retry attempts are visible as span events in
distributed traces, enabling correlation of resilience events with request flows.

---

## Recommended Pattern Composition

For inter-service HTTP calls, compose patterns in this order:

```
Bulkhead → CircuitBreaker → Retry → HTTP Call
```

1. **Bulkhead** limits concurrency to prevent thread exhaustion.
2. **Circuit breaker** fast-fails when the downstream is unhealthy.
3. **Retry** handles transient errors for calls that pass the circuit breaker.

This composition ensures retries do not overwhelm an already failing service.
