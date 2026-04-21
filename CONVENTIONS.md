# FTGO Microservices Conventions

This document is **normative**: every new microservice, shared library, and
platform component in this repository must follow it. Deviations require an
ADR in [`docs/adr/`](docs/adr/).

See [`docs/adr/0001-repository-structure.md`](docs/adr/0001-repository-structure.md)
for the rationale behind the top-level layout.

## 1. Top-Level Layout

```
ftgo-monolith/
├── services/             # Deployable microservices (one per bounded context)
├── libs/                 # Reusable JVM libraries
├── platform/             # Cross-cutting infrastructure (gateway, observability, …)
├── templates/            # Canonical skeletons to copy when adding new components
├── docs/                 # Architecture Decision Records and narrative docs
├── deployment/           # Legacy deployment assets (to be reorganized under platform/)
└── settings.gradle       # Registers every Gradle subproject
```

**Rule:** Code lives in exactly one of `services/`, `libs/`, or `platform/`.
Top-level legacy modules (`ftgo-*`) are the exception during the migration
and will be retired.

## 2. Bounded Contexts

FTGO has four bounded contexts. Each owns exactly one service directory:

| Bounded Context | Directory                     | Gradle Path                       |
|-----------------|-------------------------------|-----------------------------------|
| Consumer        | `services/consumer-service/`  | `:services:consumer-service`      |
| Order           | `services/order-service/`     | `:services:order-service`         |
| Restaurant      | `services/restaurant-service/`| `:services:restaurant-service`    |
| Courier         | `services/courier-service/`   | `:services:courier-service`       |

Adding a fifth bounded context requires an ADR justifying the split.

## 3. Service Directory Layout

Every `services/<name>-service/` directory **must** match this shape:

```
services/<name>-service/
├── build.gradle                  # Service build
├── README.md                     # Overview, owners, runbook links
├── config/
│   ├── application.yml           # Default configuration
│   └── application-<profile>.yml # Per-environment overrides
├── docker/
│   └── Dockerfile
├── k8s/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── configmap.yaml            # (optional) non-secret configuration
└── src/
    ├── main/
    │   ├── java/com/ftgo/<context>/
    │   │   ├── <Context>ServiceApplication.java    # @SpringBootApplication
    │   │   ├── config/           # @Configuration classes
    │   │   ├── web/              # @RestController classes
    │   │   ├── domain/           # Entities, repositories, domain services
    │   │   └── api/              # DTOs
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/     # Flyway scripts: V<n>__<name>.sql
    └── test/java/com/ftgo/<context>/
```

**Canonical template:** [`templates/service-template/`](templates/service-template/).
When adding a new service, copy this directory — do not hand-roll one.

## 4. Naming Conventions

### Directory names

- Services: **`<context>-service`** in kebab-case. The context is a single
  noun (`consumer`, `order`, `restaurant`, `courier`). No abbreviations.
- Libraries: **`ftgo-<name>`** in kebab-case. The `ftgo-` prefix is retained
  so artifacts published to Maven read as FTGO artifacts.
- Platform components: **`<function>`** in kebab-case (`api-gateway`,
  `config-server`).

### Gradle project paths

| Kind      | Pattern                           | Example                          |
|-----------|-----------------------------------|----------------------------------|
| Service   | `:services:<context>-service`     | `:services:order-service`        |
| API lib   | `:services:<context>-service-api` | `:services:order-service-api` *  |
| Library   | `:libs:<name>`                    | `:libs:ftgo-common`              |
| Platform  | `:platform:<function>`            | `:platform:api-gateway`          |

\* API contract modules live **inside** their owning service directory as
`services/<name>-service/<name>-service-api/`. They are the only cross-service
dependency allowed.

### Java package names

Root package for every new FTGO module:

```
com.ftgo.<context>[.<layer>]
```

- `<context>` is the bounded-context name (`consumer`, `order`, `restaurant`,
  `courier`) or a shared namespace (`common`, `common.jpa`, `test`,
  `platform.gateway`).
- `<layer>` is one of `config`, `web`, `domain`, `api`, or `infrastructure`.
- Legacy code still uses `net.chrisrichardson.ftgo.*`. Do not add new code
  under that root — it will be relocated as each module migrates.

| Module                              | Root package                |
|-------------------------------------|-----------------------------|
| `services/order-service/`           | `com.ftgo.order`            |
| `services/order-service-api/`       | `com.ftgo.order.api`        |
| `libs/ftgo-common/`                 | `com.ftgo.common`           |
| `libs/ftgo-common-jpa/`             | `com.ftgo.common.jpa`       |
| `libs/ftgo-test-util/`              | `com.ftgo.test`             |
| `platform/api-gateway/`             | `com.ftgo.platform.gateway` |

### Class names (Java)

Follow the [user-provided Java best-practices](../CONVENTIONS.md#java-coding-best-practices)
style:

| Element   | Style                  | Example                                   |
|-----------|------------------------|-------------------------------------------|
| Class     | `PascalCase`, noun     | `OrderService`, `ConsumerController`      |
| Interface | `PascalCase`           | `OrderRepository`                         |
| Method    | `camelCase`, verb-led  | `createOrder`, `findByConsumerId`         |
| Constant  | `UPPER_SNAKE_CASE`     | `MAX_LINE_ITEMS`                          |
| DTO       | Suffix the purpose     | `CreateOrderRequest`, `OrderResponse`     |
| Entry pt. | `<Context>ServiceApplication` | `OrderServiceApplication`          |

### Docker images

- Pattern: **`ftgo/<context>-service[:<tag>]`**.
- `latest` is for local dev only. Deployed tags are the Git short SHA or a
  semantic version.

### Kubernetes resource names

- `metadata.name`: **`<context>-service`** (matches the service directory).
- Required labels on every workload:
  - `app: <context>-service`
  - `bounded-context: <context>`
  - `tier: application` (for services) / `platform` (for platform/*).

### Databases / schemas

- Per-service database name: **`ftgo_<context>`** (`ftgo_order`,
  `ftgo_consumer`, …).
- Migration file name: **`V<n>__<snake_case_summary>.sql`** (Flyway default).
- Each service owns its own schema — no cross-service joins.

### Environment variables

- Spring config keys are preferred; when a raw env var is needed use
  `SCREAMING_SNAKE_CASE` with a `FTGO_` prefix: `FTGO_ORDER_DB_URL`,
  `FTGO_KAFKA_BROKERS`. Exception: well-known vars set by Spring Boot
  itself (`SPRING_PROFILES_ACTIVE`, `JAVA_OPTS`) keep their standard names.

## 5. Dependency Rules

1. **Services may depend on:**
   - Any `libs/*` module.
   - Another service's `*-service-api` module (never its full service jar).
   - Third-party artifacts.
2. **Services may NOT depend on:**
   - Another service's internals (`services/<other>-service`, excluding
     its `-api` module).
   - Any `platform/*` module at compile time (platform components are
     deployed independently).
3. **Libraries (`libs/*`) may depend on:**
   - Other `libs/*` modules (acyclically).
   - Third-party artifacts.
4. **Libraries may NOT depend on** `services/*` or `platform/*`.
5. **Platform components** are self-contained deployables; they may depend
   on `libs/*` but not on services.

Spring Boot applications live **only** in `services/*` and possibly
`platform/*` — never in `libs/*`.

## 6. Configuration

- Bundled defaults: `src/main/resources/application.yml`.
- Environment overrides: `config/application-<profile>.yml` at the service
  directory root. CI / deployment tools mount these as config-maps.
- Never commit secrets. Reference them by env var name (`${DB_PASSWORD}`).

## 7. Adding a New Service

1. `cp -r templates/service-template services/<name>-service`
2. Rename the package `com.ftgo.example` → `com.ftgo.<context>` in both
   `src/main/java` and `src/test/java`.
3. Replace `example-service` and `example` in:
   - `services/<name>-service/build.gradle`
   - `services/<name>-service/docker/Dockerfile`
   - `services/<name>-service/k8s/*.yaml`
   - `services/<name>-service/config/application.yml`
4. Register the module in `settings.gradle`:
   ```groovy
   include 'services:<name>-service'
   ```
5. Add database migrations under `src/main/resources/db/migration/`.
6. Add an ADR in `docs/adr/` if this service introduces a new bounded
   context.

## 8. Adding a New Shared Library

A new library is justified only when **two or more** services legitimately
share the code. Otherwise, inline it. See
[`libs/README.md`](libs/README.md) for the checklist and
`libs/ftgo-common/` for a canonical example.

## 9. Amending These Conventions

Propose the change via a PR that updates this file **and** adds an ADR
under `docs/adr/`. Once merged, regenerate any affected scaffolds in
`templates/` so future services pick up the new convention automatically.
