# FTGO Resilience Patterns — EM-44

> Health checks, service discovery, and resilience patterns for reliable
> inter-service communication.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Shared Resilience Library](#shared-resilience-library)
- [Health Checks](#health-checks)
- [Service Discovery](#service-discovery)
- [Resilience Patterns](#resilience-patterns)
- [Graceful Shutdown](#graceful-shutdown)
- [Kubernetes Integration](#kubernetes-integration)
- [Configuration Reference](#configuration-reference)
- [Testing](#testing)

---

## Overview

The FTGO resilience infrastructure provides a standardized set of patterns
for building reliable microservices. All services depend on the shared
`ftgo-resilience-lib` module, which provides:

1. **Health Checks** — Custom Actuator indicators for database, disk, and business health
2. **Service Discovery** — Kubernetes DNS-based resolution (no Eureka/Consul needed)
3. **Resilience Patterns** — Circuit breaker, retry, bulkhead, and rate limiter via Resilience4j
4. **Graceful Shutdown** — Zero-downtime deployments with Spring Boot + Kubernetes lifecycle hooks

## Architecture

```
┌─────────────────────────────────────────────┐
│              Kubernetes Cluster              │
│                                             │
│  ┌──────────┐  DNS   ┌──────────────────┐  │
│  │  Order   │◄──────►│   Restaurant     │  │
│  │ Service  │        │    Service       │  │
│  └────┬─────┘        └────────┬─────────┘  │
│       │                       │             │
│       │ Circuit Breaker       │ Retry       │
│       │ + Retry + Bulkhead    │ + Bulkhead  │
│       │                       │             │
│  ┌────▼─────┐        ┌───────▼──────────┐  │
│  │ Consumer │        │    Courier       │  │
│  │ Service  │        │    Service       │  │
│  └──────────┘        └──────────────────┘  │
│                                             │
│  shared/ftgo-resilience-lib (all services)  │
└─────────────────────────────────────────────┘
```

## Shared Resilience Library

**Module:** `shared/ftgo-resilience-lib`

Add to any microservice:

```groovy
// build.gradle
dependencies {
    implementation project(":shared:ftgo-resilience-lib")
}
```

The library auto-configures all resilience components via Spring Boot
auto-configuration. No additional `@Import` or `@Enable*` annotations needed.

### Key Classes

| Class | Purpose |
|-------|---------|
| `FtgoResilienceAutoConfiguration` | Entry point; imports all config classes |
| `ResilienceConfiguration` | Resilience4j registry beans with FTGO defaults |
| `GracefulShutdownConfiguration` | Tomcat connector customization for graceful shutdown |
| `ServiceDiscoveryConfiguration` | K8s DNS service registry bean |
| `KubernetesServiceRegistry` | Resolves service names to cluster-internal URLs |
| `DatabaseHealthIndicator` | Validates DB connectivity via `SELECT 1` |
| `DiskSpaceHealthIndicator` | Monitors free disk space against a threshold |
| `BusinessHealthIndicator` | Abstract base for service-specific health checks |

## Health Checks

### Built-in Indicators

| Indicator | Key | Checks |
|-----------|-----|--------|
| Database | `ftgoDatabase` | Executes `SELECT 1`, reports DB product/version |
| Disk Space | `ftgoDiskSpace` | Free space vs. configurable threshold (default: 100 MB) |

### Per-Service Business Health

Each service extends `BusinessHealthIndicator` for domain-specific checks:

| Service | Component Key | Checks |
|---------|--------------|--------|
| Order | `orderProcessing` | Order pipeline operational |
| Consumer | `consumerManagement` | Registration system operational |
| Restaurant | `restaurantManagement` | Restaurant operations operational |
| Courier | `courierDispatch` | Courier dispatch operational |

### Health Groups

Health indicators are grouped for Kubernetes probes:

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
      group:
        liveness:
          include: livenessState,ftgoDiskSpace
        readiness:
          include: readinessState,ftgoDatabase,<businessIndicator>
```

- **Liveness** (`/actuator/health/liveness`): Checks if the process is alive (disk space)
- **Readiness** (`/actuator/health/readiness`): Checks if the service can handle traffic (DB + business)

## Service Discovery

### Kubernetes DNS

Services are discovered via Kubernetes DNS. No external service registry needed.

**Pattern:** `<service-name>.<namespace>.svc.cluster.local:<port>`

```java
@Autowired
private KubernetesServiceRegistry serviceRegistry;

// Returns: http://ftgo-restaurant-service.ftgo.svc.cluster.local:8080
String url = serviceRegistry.getServiceUrl("ftgo-restaurant-service");
```

### Configuration

```yaml
ftgo:
  service-discovery:
    namespace: ftgo              # K8s namespace
    cluster-domain: cluster.local # K8s cluster domain
    default-port: 8080           # Default service port
    default-scheme: http         # http or https
```

## Resilience Patterns

All patterns are provided by [Resilience4j](https://resilience4j.readme.io/) v2.2.0.

### Circuit Breaker

Prevents cascading failures by short-circuiting calls to failing services.

| Parameter | Default | Description |
|-----------|---------|-------------|
| `failureRateThreshold` | 50% | % failures to open the circuit |
| `slowCallRateThreshold` | 80% | % slow calls to open the circuit |
| `slowCallDurationThreshold` | 2s | Threshold for "slow" calls |
| `waitDurationInOpenState` | 30s | Time before attempting half-open |
| `slidingWindowSize` | 10 | Number of calls in the window |
| `minimumNumberOfCalls` | 5 | Min calls before evaluating |
| `permittedNumberOfCallsInHalfOpenState` | 3 | Test calls in half-open |

**State Machine:**
```
CLOSED ──(failure rate exceeded)──► OPEN
  ▲                                   │
  │                          (wait duration)
  │                                   ▼
  └───(success rate OK)──── HALF_OPEN
```

### Retry with Exponential Backoff

Retries transient failures with increasing delays.

| Parameter | Default | Description |
|-----------|---------|-------------|
| `maxAttempts` | 3 | Total attempts (initial + retries) |
| `waitDuration` | 500ms | Initial wait between retries |
| `exponentialBackoffMultiplier` | 2.0 | Backoff multiplier |
| `retryExceptions` | IOException, SocketTimeoutException, ResourceAccessException | Exceptions to retry |
| `ignoreExceptions` | IllegalArgumentException | Exceptions to NOT retry |

**Timeline:** `500ms → 1000ms → 2000ms` (3 attempts total)

### Bulkhead

Limits concurrent calls to prevent resource exhaustion.

| Parameter | Default | Description |
|-----------|---------|-------------|
| `maxConcurrentCalls` | 25 | Max parallel executions |
| `maxWaitDuration` | 500ms | Max time to wait for a permit |

### Rate Limiter

Controls request throughput to protect downstream services.

| Parameter | Default | Description |
|-----------|---------|-------------|
| `limitForPeriod` | 50 | Calls allowed per period |
| `limitRefreshPeriod` | 1s | Period duration |
| `timeoutDuration` | 500ms | Max time to wait for permission |

### Service-Specific Overrides

Override defaults in each service's `application.yml`:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      restaurantService:
        failureRateThreshold: 60
        waitDurationInOpenState: 60s
  retry:
    instances:
      restaurantService:
        maxAttempts: 5
```

## Graceful Shutdown

Ensures zero-downtime during rolling deployments.

### Timeline

```
1. K8s removes pod from Service endpoints
2. preStop hook: sleep 10 (allows LB to drain)
3. SIGTERM sent to JVM
4. Spring Boot graceful shutdown begins
5. In-flight requests complete (up to 30s)
6. Application stops
7. K8s terminates pod (terminationGracePeriodSeconds: 30)
```

### Configuration

```yaml
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

### Kubernetes Manifest

```yaml
spec:
  terminationGracePeriodSeconds: 30
  containers:
    - lifecycle:
        preStop:
          exec:
            command: ["sh", "-c", "sleep 10"]
```

## Kubernetes Integration

### Probes

All services are configured with three probe types:

| Probe | Path | Purpose | Timing |
|-------|------|---------|--------|
| **Startup** | `/actuator/health` | Wait for app initialization | delay=10s, period=5s, threshold=30 |
| **Liveness** | `/actuator/health/liveness` | Detect deadlocks/hangs | delay=60s, period=10s, threshold=3 |
| **Readiness** | `/actuator/health/readiness` | Traffic routing control | delay=30s, period=5s, threshold=3 |

### Deployment Strategy

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1
    maxUnavailable: 0  # Zero-downtime
```

## Configuration Reference

### Resilience Properties

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.service-discovery.namespace` | `ftgo` | K8s namespace |
| `ftgo.service-discovery.cluster-domain` | `cluster.local` | K8s cluster DNS domain |
| `ftgo.service-discovery.default-port` | `8080` | Default service port |
| `ftgo.service-discovery.default-scheme` | `http` | URL scheme |
| `ftgo.graceful-shutdown.enabled` | `true` | Enable graceful shutdown config |
| `ftgo.health.disk-space.threshold-mb` | `100` | Disk space threshold (MB) |
| `ftgo.health.disk-space.path` | `/` | Disk path to monitor |

### Gradle Dependencies

Add to `build.gradle`:
```groovy
implementation project(":shared:ftgo-resilience-lib")
implementation 'org.springframework.boot:spring-boot-starter-aop'  // Required for Resilience4j annotations
```

## Testing

### Unit Tests

Located in `shared/ftgo-resilience-lib/src/test/`:
- `ResilienceConfigurationTest` — Validates registry defaults
- `KubernetesServiceRegistryTest` — Validates DNS URL resolution
- `DiskSpaceHealthIndicatorTest` — Validates disk space checks

### Integration Tests

Located in `shared/ftgo-resilience-lib/src/integration-test/`:
- `ResilienceAutoConfigurationIntegrationTest` — Full Spring context test

Run integration tests:
```bash
./gradlew :shared:ftgo-resilience-lib:integrationTest
```

### Verifying Health Endpoints

```bash
# All health details
curl http://localhost:8080/actuator/health | jq .

# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness
```
