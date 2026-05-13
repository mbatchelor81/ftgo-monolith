# FTGO Platform — Observability Guide

## Overview

The FTGO platform uses **Micrometer 1.12.5** with **Prometheus** for metrics collection
and **Grafana** for visualization. Each microservice exposes metrics via the Spring Boot
Actuator `/actuator/prometheus` endpoint.

## Architecture

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ Order Service│    │Consumer Svc  │    │Restaurant Svc│    │Courier Svc   │
│  :8080       │    │  :8080       │    │  :8080       │    │  :8080       │
│  /actuator/  │    │  /actuator/  │    │  /actuator/  │    │  /actuator/  │
│  prometheus  │    │  prometheus  │    │  prometheus  │    │  prometheus  │
└──────┬───────┘    └──────┬───────┘    └──────┬───────┘    └──────┬───────┘
       │                   │                   │                   │
       └───────────────────┴───────────────────┴───────────────────┘
                                    │
                            ┌───────┴───────┐
                            │  Prometheus   │
                            │  :9090        │
                            └───────┬───────┘
                                    │
                            ┌───────┴───────┐
                            │   Grafana     │
                            │   :3000       │
                            └───────────────┘
```

## Components

### ftgo-metrics-lib

Shared library providing auto-configured Micrometer metrics for all services.

**Location:** `libs/ftgo-metrics-lib/`

**What it provides:**
- Auto-configuration via Spring Boot's `AutoConfiguration.imports`
- Custom business metrics per service domain
- Common platform-level metrics (API requests, errors, latency)

**Integration:** Add to any service's `build.gradle`:
```groovy
implementation project(":ftgo-metrics-lib")
```

### Prometheus

Scrapes metrics from application services every 10 seconds (global default 15s). Configuration is at
`infrastructure/monitoring/prometheus/prometheus.yml`.

**Scrape targets:**
| Service | Endpoint | Job Name |
|---------|----------|----------|
| Order Service | `order-service:8080/actuator/prometheus` | ftgo-order-service |
| Consumer Service | `consumer-service:8080/actuator/prometheus` | ftgo-consumer-service |
| Restaurant Service | `restaurant-service:8080/actuator/prometheus` | ftgo-restaurant-service |
| Courier Service | `courier-service:8080/actuator/prometheus` | ftgo-courier-service |

### Grafana Dashboards

Three pre-configured dashboards provisioned automatically:

1. **FTGO — Service Overview** (`ftgo-service-overview`)
   - Service up/down status
   - Request rates, error rates, P50/P95 latency per service

2. **FTGO — JVM Metrics** (`ftgo-jvm-metrics`)
   - Heap/non-heap memory usage
   - GC pause duration and frequency
   - Thread counts, CPU usage

3. **FTGO — Business KPIs** (`ftgo-business-kpis`)
   - Order lifecycle rates (created, approved, rejected, delivered)
   - Consumer registrations and validations
   - Restaurant ticket pipeline
   - Courier operations and delivery times
   - Rejection rate gauge, processing/delivery time gauges

### Alerting Rules

Defined in `infrastructure/monitoring/prometheus/alert-rules.yml`:

| Alert | Condition | Severity |
|-------|-----------|----------|
| ServiceDown | Service unreachable for 1m | critical |
| HighErrorRate | 5xx rate > 5% for 5m | warning |
| HighLatency | P95 > 2s for 5m | warning |
| HighHeapUsage | Heap > 90% for 5m | warning |
| HighGCPause | Avg GC pause > 500ms for 5m | warning |
| HighOrderRejectionRate | Rejection > 20% for 10m | warning |
| NoOrdersCreated | Zero orders for 30m | warning |
| CourierShortage | < 2 couriers available for 15m | warning |

## Running the Monitoring Stack

```bash
cd infrastructure/monitoring
docker-compose -f docker-compose.monitoring.yml up -d
```

**Access:**
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin / admin)
- Alertmanager: http://localhost:9093

## Custom Business Metrics Reference

See `libs/ftgo-metrics-lib/README.md` for the full list of custom metrics
per service domain.

## Service Configuration

Each service should expose the Prometheus actuator endpoint:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    tags:
      application: ${spring.application.name}
```

This configuration is already present in all new microservice `application.yml` files.
