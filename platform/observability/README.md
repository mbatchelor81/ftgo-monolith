# Observability

Metrics, traces, and structured-log pipelines consumed by every FTGO service.

| Subdirectory | Purpose                                                     | Related Ticket |
|--------------|-------------------------------------------------------------|----------------|
| `prometheus/`| Prometheus scrape config + alert rules                      | EM-41          |
| `grafana/`   | Pre-built dashboards per service + provisioning definitions | EM-41          |
| `tracing/`   | Zipkin / Jaeger deployment + Sleuth config                  | EM-42          |
| `logging/`   | ELK/EFK stack deployment + log shipper config               | EM-43, EM-49   |

## Metrics (EM-41)

Every service applies the `ftgo.observability-conventions` precompiled Gradle
plugin (see `build-logic/convention/src/main/groovy/`), which brings in:

* `spring-boot-starter-actuator` — health, info, and management endpoints
* `micrometer-core` — metrics facade used by service code
* `micrometer-registry-prometheus` — text-format registry scraped by Prometheus

### Endpoint layout

Metrics are exposed on a **separate management port** (`9090`) so the
Prometheus / health surface area is never reachable through the public
service port. Operators are expected to firewall 9090 at the network layer
(Kubernetes NetworkPolicy or cloud security groups) and only expose it to
Prometheus, Grafana Agent, or kubelet probes.

| Service            | Public port | Management port | Host port (compose dev) |
|--------------------|-------------|-----------------|-------------------------|
| consumer-service   | 8080        | 9090            | 8082 / 9092             |
| order-service      | 8080        | 9090            | 8083 / 9093             |
| restaurant-service | 8080        | 9090            | 8084 / 9094             |
| courier-service    | 8080        | 9090            | 8085 / 9095             |

The port split is configured in `services/*/config/application.yml` under
`management.server.port` and overridable via `MANAGEMENT_SERVER_PORT`.

### Business metrics

Each service registers its own set of counters and timers at startup so
Grafana panels never report "No data" during a cold start. See
`services/<name>/src/main/java/com/ftgo/<name>/metrics/` for the concrete
instruments. Naming follows the dot-delimited Micrometer convention; the
Prometheus scraper translates dots to underscores.

| Service            | Metric (Micrometer)                  | Prometheus series (counter)                        |
|--------------------|--------------------------------------|----------------------------------------------------|
| order-service      | `orders.created`                     | `orders_created_total`                             |
|                    | `orders.cancelled`                   | `orders_cancelled_total`                           |
|                    | `orders.revised`                     | `orders_revised_total`                             |
|                    | `orders.delivered`                   | `orders_delivered_total`                           |
|                    | `orders.processing.time`             | `orders_processing_time_seconds_*`                 |
| consumer-service   | `consumers.registered`               | `consumers_registered_total`                       |
|                    | `consumers.order.validated`          | `consumers_order_validated_total`                  |
|                    | `consumers.order.validation.failed`  | `consumers_order_validation_failed_total`          |
| restaurant-service | `restaurants.created`                | `restaurants_created_total`                        |
|                    | `restaurants.menu.updated`           | `restaurants_menu_updated_total`                   |
|                    | `restaurants.orders.accepted`        | `restaurants_orders_accepted_total`                |
|                    | `restaurants.order.preparation.time` | `restaurants_order_preparation_time_seconds_*`     |
| courier-service    | `couriers.registered`                | `couriers_registered_total`                        |
|                    | `couriers.availability.changed`      | `couriers_availability_changed_total`              |
|                    | `couriers.deliveries.completed`      | `couriers_deliveries_completed_total`              |
|                    | `couriers.delivery.duration`         | `couriers_delivery_duration_seconds_*`             |

### Local development

Bring up the full stack with Prometheus and Grafana:

```bash
docker compose -f docker-compose.dev.yml up --build
```

Then open:

* Prometheus — <http://localhost:9090>
* Grafana    — <http://localhost:3000>  (default `admin` / `admin`)

The "FTGO — Services Overview" dashboard is auto-provisioned from
`grafana/dashboards/ftgo-services-overview.json`.

### Hardening checklist (production)

1. **Never** route `management.server.port` through the Ingress or public
   load balancer. Keep it on the pod-internal network only.
2. Add a `NetworkPolicy` that allows ingress to port 9090 only from the
   Prometheus namespace (or kubelet for probe paths).
3. Rotate `GF_SECURITY_ADMIN_PASSWORD` away from the dev default and wire
   Grafana to SSO before any non-local deployment.
4. Keep `management.endpoints.web.exposure.include` limited to
   `health,info,prometheus`. Do not add `env`, `beans`, `configprops`, or
   `heapdump` in production.
