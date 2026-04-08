# Observability & Metrics — FTGO Microservices

> **Jira**: EM-41 — Upgrade Micrometer/Prometheus Metrics and Add Service-Level Dashboards

## Overview

Every FTGO microservice exposes application, JVM, and custom business metrics via
[Micrometer](https://micrometer.io/) with a Prometheus registry. Metrics are scraped
by Prometheus and visualised in pre-built Grafana dashboards.

| Component   | Version   | Port  | Purpose                              |
|-------------|-----------|-------|--------------------------------------|
| Micrometer  | 1.12.4    | —     | In-process metrics instrumentation   |
| Prometheus  | 2.51.0    | 9090  | Time-series metrics storage & query  |
| Grafana     | 10.4.1    | 3000  | Dashboard visualisation & alerting   |

---

## Architecture

```
┌─────────────────┐   ┌─────────────────┐   ┌──────────────────────┐
│ Order Service    │   │ Consumer Service │   │ Restaurant Service   │
│ :8081            │   │ :8082            │   │ :8083                │
│ /actuator/prom.  │   │ /actuator/prom.  │   │ /actuator/prom.      │
└────────┬────────┘   └────────┬────────┘   └──────────┬───────────┘
         │                     │                       │
         └─────────┬───────────┴───────────┬───────────┘
                   │                       │
         ┌────────▼────────┐     ┌────────▼────────┐
         │ Courier Service │     │    Prometheus    │◄── scrapes /15s
         │ :8084           │     │    :9090         │
         │ /actuator/prom. │     └────────┬────────┘
         └─────────────────┘              │
                                 ┌────────▼────────┐
                                 │     Grafana      │
                                 │     :3000        │
                                 └─────────────────┘
```

---

## Quick Start

```bash
# Start all services including Prometheus and Grafana
docker compose -f docker-compose.dev.yml up -d

# Open dashboards
open http://localhost:3000          # Grafana  (admin / admin)
open http://localhost:9090          # Prometheus UI
open http://localhost:8081/actuator/prometheus  # Order Service raw metrics
```

---

## Convention Plugin

All microservices receive Actuator + Micrometer + Prometheus via the
`ftgo.observability-conventions` Gradle convention plugin, which is automatically
applied by `FtgoMicroservicePlugin`.

**What it provides:**
- `spring-boot-starter-actuator`
- `micrometer-registry-prometheus` (version from `libs.versions.toml`)

**Exposed actuator endpoints** (configured in each service's `application.yml`):

| Endpoint                | Purpose                    |
|-------------------------|----------------------------|
| `/actuator/health`      | Health checks              |
| `/actuator/info`        | Build / app info           |
| `/actuator/metrics`     | Micrometer metric listing  |
| `/actuator/prometheus`  | Prometheus scrape endpoint |

---

## Custom Business Metrics

Each service defines its own `*MetricsConfiguration` class under
`services/<service>/src/main/java/com/ftgo/<domain>/config/`.

### Order Service (`ftgo-order-service`)

| Metric                                | Type    | Description                                      |
|---------------------------------------|---------|--------------------------------------------------|
| `ftgo.orders.created`                 | Counter | Total orders created                             |
| `ftgo.orders.cancelled`               | Counter | Total orders cancelled                           |
| `ftgo.orders.revised`                 | Counter | Total orders revised                             |
| `ftgo.orders.accepted`                | Counter | Total orders accepted by restaurants             |
| `ftgo.orders.delivered`               | Counter | Total orders delivered                           |
| `ftgo.orders.active`                  | Gauge   | Currently active (non-terminal) orders           |
| `ftgo.orders.processing.duration`     | Timer   | Order processing latency (p50/p95/p99)           |

### Consumer Service (`ftgo-consumer-service`)

| Metric                                | Type    | Description                                      |
|---------------------------------------|---------|--------------------------------------------------|
| `ftgo.consumers.registered`           | Counter | Total consumers registered                       |
| `ftgo.consumers.validated`            | Counter | Total consumer order validations                 |
| `ftgo.consumers.validation.failed`    | Counter | Total failed consumer validations                |
| `ftgo.consumers.total`                | Gauge   | Total registered consumers                       |
| `ftgo.consumers.validation.duration`  | Timer   | Consumer validation latency (p50/p95/p99)        |

### Restaurant Service (`ftgo-restaurant-service`)

| Metric                                          | Type    | Description                              |
|-------------------------------------------------|---------|------------------------------------------|
| `ftgo.restaurants.created`                      | Counter | Total restaurants created                |
| `ftgo.restaurants.menu.revised`                 | Counter | Total menu revisions                     |
| `ftgo.restaurants.tickets.accepted`             | Counter | Total tickets accepted                   |
| `ftgo.restaurants.tickets.preparing`            | Counter | Total tickets moved to preparing         |
| `ftgo.restaurants.total`                        | Gauge   | Total registered restaurants             |
| `ftgo.restaurants.ticket.processing.duration`   | Timer   | Ticket processing latency (p50/p95/p99)  |

### Courier Service (`ftgo-courier-service`)

| Metric                                    | Type    | Description                                  |
|-------------------------------------------|---------|----------------------------------------------|
| `ftgo.couriers.created`                   | Counter | Total couriers created                       |
| `ftgo.couriers.availability.changed`      | Counter | Total courier availability state changes     |
| `ftgo.couriers.deliveries.assigned`       | Counter | Total deliveries assigned                    |
| `ftgo.couriers.deliveries.completed`      | Counter | Total deliveries completed                   |
| `ftgo.couriers.available`                 | Gauge   | Currently available couriers                 |
| `ftgo.couriers.delivery.duration`         | Timer   | Delivery duration latency (p50/p95/p99)      |

---

## Grafana Dashboards

Three pre-provisioned dashboards are auto-loaded into Grafana on startup:

| Dashboard              | UID                     | Description                                     |
|------------------------|-------------------------|-------------------------------------------------|
| FTGO — RED Metrics     | `ftgo-red-metrics`      | Request Rate, Error Rate, Duration per service   |
| FTGO — JVM Metrics     | `ftgo-jvm-metrics`      | Heap, GC, Threads, CPU, HikariCP per service     |
| FTGO — Business Metrics| `ftgo-business-metrics` | Custom counters/gauges/timers per service        |

All dashboards support:
- **Template variable `$service`** — filter by one or all services
- **Auto-refresh** — 30-second default interval
- **Table legends** with mean/max calculations

### Dashboard files

Located at `monitoring/grafana/dashboards/`:
- `ftgo-red-metrics.json`
- `ftgo-jvm-metrics.json`
- `ftgo-business-metrics.json`

---

## Prometheus Configuration

### Scrape Config

File: `monitoring/prometheus/prometheus.yml`

- **Global scrape interval**: 15 seconds
- **One job per service**: `ftgo-order-service`, `ftgo-consumer-service`,
  `ftgo-restaurant-service`, `ftgo-courier-service`
- **Metrics path**: `/actuator/prometheus`
- **Labels**: `service`, `domain` per target

### Alert Rules

File: `monitoring/prometheus/alert-rules.yml`

| Alert Group            | Alert Name                        | Severity | Trigger                                          |
|------------------------|-----------------------------------|----------|--------------------------------------------------|
| Service Health         | `ServiceDown`                     | critical | Service unreachable > 1 minute                   |
| Service Health         | `HighRestartRate`                 | warning  | Uptime < 5 minutes (frequent restarts)           |
| RED Metrics            | `HighErrorRate`                   | critical | HTTP 5xx rate > 5% for 5 minutes                 |
| RED Metrics            | `HighLatencyP95`                  | warning  | p95 response time > 1 second for 5 minutes       |
| RED Metrics            | `HighLatencyP99`                  | critical | p99 response time > 2 seconds for 5 minutes      |
| RED Metrics            | `LowRequestRate`                  | warning  | Zero requests for 10 minutes                     |
| JVM                    | `HighJvmMemoryUsage`              | warning  | Heap usage > 85% for 5 minutes                   |
| JVM                    | `CriticalJvmMemoryUsage`          | critical | Heap usage > 95% for 2 minutes                   |
| JVM                    | `HighGcPauseTime`                 | warning  | Average GC pause > 500ms                         |
| JVM                    | `HighThreadCount`                 | warning  | Live threads > 300                               |
| Business               | `HighOrderCancellationRate`       | warning  | Cancellation rate > 20% over 15 minutes          |
| Business               | `NoCouriersAvailable`             | warning  | Zero available couriers for 5 minutes            |
| Business               | `ConsumerValidationFailureSpike`  | warning  | Validation failure rate > 0.5/sec for 5 minutes  |

---

## Docker Compose Services

Added to `docker-compose.dev.yml`:

```yaml
prometheus:
  image: prom/prometheus:v2.51.0
  ports: ["9090:9090"]
  # Mounts prometheus.yml and alert-rules.yml
  # 15-day retention, lifecycle and admin API enabled

grafana:
  image: grafana/grafana:10.4.1
  ports: ["3000:3000"]
  # Auto-provisions Prometheus datasource and dashboards
  # Default credentials: admin / admin
```

### Volumes

| Volume           | Purpose                    |
|------------------|----------------------------|
| `prometheus-data`| Prometheus TSDB storage    |
| `grafana-data`   | Grafana state & plugins    |

---

## Adding Metrics to a New Service

1. Apply `FtgoMicroservicePlugin` in your `build.gradle` — Actuator + Micrometer are
   included automatically via `ftgo.observability-conventions`.

2. Create a `*MetricsConfiguration` class in `src/main/java/com/ftgo/<domain>/config/`:

   ```java
   @Configuration
   public class MyMetricsConfiguration {

       @Bean
       public Counter myOperationCounter(MeterRegistry registry) {
           return Counter.builder("ftgo.myservice.operations")
                   .description("Total operations processed")
                   .tag("service", "ftgo-my-service")
                   .register(registry);
       }
   }
   ```

3. Add a scrape job in `monitoring/prometheus/prometheus.yml`:

   ```yaml
   - job_name: "ftgo-my-service"
     metrics_path: /actuator/prometheus
     static_configs:
       - targets: ["ftgo-my-service:8080"]
         labels:
           service: ftgo-my-service
           domain: mydomain
   ```

4. Add panels to the appropriate Grafana dashboard JSON, or create a new one
   under `monitoring/grafana/dashboards/`.

5. Add alerting rules for your service metrics in
   `monitoring/prometheus/alert-rules.yml`.

---

## Useful PromQL Queries

```promql
# Request rate per service
sum by (job) (rate(http_server_requests_seconds_count[5m]))

# Error rate (5xx) per service
sum by (job) (rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
  / sum by (job) (rate(http_server_requests_seconds_count[5m]))

# p95 latency per service
histogram_quantile(0.95,
  sum by (job, le) (rate(http_server_requests_seconds_bucket[5m])))

# Heap usage percentage
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}

# Order creation rate
rate(ftgo_orders_created_total[5m])

# Available couriers
ftgo_couriers_available
```
