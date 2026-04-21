# FTGO Service Template

Use this directory as the starting point for a new FTGO microservice. Copy it
into `services/<name>-service/`, then replace every `example`, `Example`, and
`example-service` placeholder with the new service's bounded-context name.

## Standard Layout

```
services/<name>-service/
├── build.gradle                  # Service build script
├── README.md                     # Service overview, runbook links, owners
├── config/
│   ├── application.yml           # Default (local) configuration
│   ├── application-dev.yml       # Development profile overrides
│   └── application-prod.yml      # Production profile overrides
├── docker/
│   └── Dockerfile                # Container image definition
├── k8s/
│   ├── deployment.yaml           # Kubernetes Deployment manifest
│   ├── service.yaml              # Kubernetes Service manifest
│   └── configmap.yaml            # Non-secret environment configuration
└── src/
    ├── main/
    │   ├── java/com/ftgo/<context>/
    │   │   ├── ServiceApplication.java   # @SpringBootApplication entry point
    │   │   ├── config/                   # @Configuration classes
    │   │   ├── web/                      # @RestController classes
    │   │   ├── domain/                   # Entities, repositories, domain services
    │   │   └── api/                      # DTOs and request/response models
    │   └── resources/
    │       ├── application.yml           # Bundled default configuration
    │       └── db/migration/             # Flyway SQL migrations (V1__*.sql, ...)
    └── test/
        └── java/com/ftgo/<context>/      # Unit and integration tests
```

## Checklist for a New Service

1. Copy `templates/service-template/` → `services/<name>-service/`.
2. Rename the package `com.ftgo.example` → `com.ftgo.<context>` under both
   `src/main/java` and `src/test/java`.
3. Replace `example-service` / `example` in `build.gradle`, `Dockerfile`,
   `k8s/*.yaml`, and `config/application.yml`.
4. Register the module in the root `settings.gradle`
   (`include 'services:<name>-service'`).
5. Add the service's schema migrations under
   `src/main/resources/db/migration/` using the `V<n>__<name>.sql` convention.
6. Add dependencies on shared libraries from `libs/` as needed (avoid depending
   on another service's internals — only its `*-api` module).
7. Add the service to the platform's deployment pipeline (GitHub Actions,
   container registry, k8s manifests) — see `platform/README.md`.

## Naming Rules (Short Form)

See [`CONVENTIONS.md`](../../CONVENTIONS.md) for the full list.

- Directory: `services/<context>-service/`
- Gradle project path: `:services:<context>-service`
- Java root package: `com.ftgo.<context>`
- Docker image: `ftgo/<context>-service`
- Kubernetes resource name: `<context>-service`
