# FTGO Monitoring Stack

Prometheus + Grafana monitoring for FTGO microservices.

## Quick Start

```bash
# From the repository root
docker-compose -f monitoring/docker-compose.yml up -d
```

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin / admin)

## Dashboards

| Dashboard | Description |
|-----------|-------------|
| FTGO — RED Metrics | Request rate, error rate, and duration across all services |
| FTGO — JVM Metrics | Heap/non-heap memory, GC pauses, threads, CPU usage |
| FTGO — Business Metrics | Orders, consumers, restaurants, couriers business KPIs |

## Alerting Rules

| Alert | Severity | Trigger |
|-------|----------|---------|
| ServiceDown | critical | Service unreachable for 1 min |
| HighErrorRate | critical | 5xx rate > 5% for 5 min |
| HighLatencyP95 | warning | p95 latency > 1s for 5 min |
| HighJvmHeapUsage | warning | Heap > 85% for 5 min |
| CriticalJvmHeapUsage | critical | Heap > 95% for 2 min |
| HighGcPauseTime | warning | Avg GC pause > 500ms for 5 min |
| NoOrdersCreated | warning | Zero orders in 15 min |
| HighOrderCancellationRate | warning | Cancellation rate > 20% for 15 min |

## Service Endpoints

Each FTGO microservice exposes metrics at `/actuator/prometheus` when the
`observability` Spring profile is active.
