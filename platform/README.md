# FTGO Platform

This directory owns the **cross-cutting infrastructure** that every FTGO
microservice depends on but no single bounded context should own.

Anything here must be either (a) consumed by two or more services, or (b)
responsible for the boundary between the FTGO system and the outside world
(ingress, auth, observability).

## Layout

```
platform/
├── api-gateway/           # Edge proxy: routing, auth, rate limiting (EM-38)
├── config-server/         # Centralized configuration (Spring Cloud Config or equiv.)
├── service-discovery/     # Service registry + client config (EM-44)
├── observability/         # Metrics, tracing, logging stacks (EM-41/42/43/49)
│   ├── prometheus/
│   ├── grafana/
│   ├── tracing/
│   └── logging/
└── shared-infrastructure/ # Reusable k8s primitives (namespaces, network policies)
    └── k8s/
```

Each subdirectory is self-contained and owned by the Platform team. Services
under `services/` consume these components — they do not duplicate them.

## What does NOT belong here

- Business logic for a specific bounded context. That goes in
  `services/<name>-service/`.
- Reusable Java/JVM code. That goes in `libs/` (see
  [`../libs/README.md`](../libs/README.md)).
- Database migrations for a specific service. Those live next to the
  service in `services/<name>-service/src/main/resources/db/migration/`.

## Related ADRs

- [`docs/adr/0001-repository-structure.md`](../docs/adr/0001-repository-structure.md)
  — decides mono-repo layout with `services/`, `libs/`, and `platform/`.
