# Observability

Metrics, traces, and structured-log pipelines consumed by every FTGO service.

| Subdirectory | Purpose                                            | Related Ticket |
|--------------|----------------------------------------------------|----------------|
| `prometheus/`| Prometheus scrape config + alert rules             | EM-41          |
| `grafana/`   | Pre-built dashboards per service                   | EM-41          |
| `tracing/`   | Zipkin / Jaeger deployment + Sleuth config         | EM-42          |
| `logging/`   | ELK/EFK stack deployment + log shipper config      | EM-43, EM-49   |

**Scaffold only** — manifest files will land as each ticket completes.
