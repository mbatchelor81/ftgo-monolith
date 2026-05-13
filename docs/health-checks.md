# Health Checks and Probes

## Overview

FTGO services expose health information through Spring Boot Actuator endpoints.
These endpoints are consumed by Kubernetes probes, Consul health checks, and
monitoring dashboards to determine service availability.

## Actuator Endpoints

### Endpoint Configuration

Services should expose the following actuator endpoints:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  health:
    circuitbreakers:
      enabled: true
```

### Available Endpoints

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Aggregate health status |
| `/actuator/health/liveness` | Kubernetes liveness probe |
| `/actuator/health/readiness` | Kubernetes readiness probe |
| `/actuator/health/orderService` | Order service circuit breaker health |
| `/actuator/health/consumerService` | Consumer service circuit breaker health |
| `/actuator/health/restaurantService` | Restaurant service circuit breaker health |
| `/actuator/health/courierService` | Courier service circuit breaker health |

## Health Indicator Details

### Built-in Indicators (Spring Boot)

- **`db`** — Database connectivity (DataSource auto-detected)
- **`diskSpace`** — Available disk space

### FTGO Custom Indicators (ftgo-resilience-lib)

Each service has a dedicated health indicator backed by its Resilience4j circuit
breaker. The indicator reports:

```json
{
  "status": "UP",
  "details": {
    "service": "order-service",
    "circuitBreaker.state": "CLOSED",
    "circuitBreaker.failureRate": -1.0,
    "circuitBreaker.bufferedCalls": 0,
    "circuitBreaker.failedCalls": 0,
    "circuitBreaker.successfulCalls": 0
  }
}
```

When the circuit breaker opens (state = `OPEN`), the indicator status becomes `DOWN`.

## Kubernetes Probes

### Probe Configuration

All FTGO service deployments include standardized probes:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: http
  initialDelaySeconds: 60
  periodSeconds: 15
  timeoutSeconds: 5
  failureThreshold: 3

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: http
  initialDelaySeconds: 30
  periodSeconds: 10
  timeoutSeconds: 3
  failureThreshold: 3

startupProbe:
  httpGet:
    path: /actuator/health/liveness
    port: http
  initialDelaySeconds: 10
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 12
```

### Probe Semantics

| Probe | Purpose | Failure Action |
|-------|---------|----------------|
| **Startup** | Wait for JVM/Spring context initialization | Pod stays in `Starting` state |
| **Liveness** | Detect deadlocks or unrecoverable failures | Pod is restarted |
| **Readiness** | Verify service can handle requests | Pod removed from Service endpoints |

### Probe Group Mapping

Spring Boot maps health indicator groups to probes:

- **Liveness group:** Core application health (JVM, thread deadlock detection)
- **Readiness group:** Includes database connectivity, circuit breaker states

When a circuit breaker opens, the readiness probe fails, causing Kubernetes to
stop sending traffic to the pod. Once the circuit transitions to half-open and
probes succeed, traffic is restored automatically.

### Timing Rationale

| Parameter | Value | Rationale |
|-----------|-------|-----------|
| Startup `initialDelaySeconds` | 10 | Allow JVM to begin initialization |
| Startup `failureThreshold` | 12 | Allow up to 70s for Spring context (10 + 12*5) |
| Liveness `initialDelaySeconds` | 60 | After startup probe succeeds |
| Readiness `initialDelaySeconds` | 30 | Ready before liveness kicks in |

## Integration Matrix

| Consumer | Endpoint | Frequency | Action on Failure |
|----------|----------|-----------|-------------------|
| Kubernetes liveness | `/actuator/health/liveness` | 15s | Restart pod |
| Kubernetes readiness | `/actuator/health/readiness` | 10s | Remove from endpoints |
| Kubernetes startup | `/actuator/health/liveness` | 5s | Wait for startup |
| Consul health check | `/actuator/health` | 15s | Mark critical, deregister at 90s |
| Prometheus | `/actuator/prometheus` | 15s | Alert on scrape failure |
