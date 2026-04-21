# FTGO Container Images

This document describes how FTGO microservice container images are built,
tagged, scanned, and distributed during the strangler-fig migration from
the monolith to per-service deployments.

## Registry

All service images are published to **GitHub Container Registry (GHCR)**
under the org namespace:

```
ghcr.io/mbatchelor81/ftgo/<service>:<tag>
```

where `<service>` is one of `consumer-service`, `order-service`,
`restaurant-service`, or `courier-service`.

The slash-delimited `ftgo/<service>` path matches the naming pattern
mandated by [`CONVENTIONS.md`](../CONVENTIONS.md) (**"Pattern:
`ftgo/<context>-service[:<tag>]`"**). GHCR supports nested package
paths, so both the registry push and the `actions/delete-package-versions`
cleanup job reference the package as `ftgo/<service>`.

GHCR was chosen because:

- It is free for public images and generously priced for private ones.
- Authentication uses the built-in `GITHUB_TOKEN` on Actions — no extra
  secrets to rotate.
- Package retention policies can be enforced via
  [`actions/delete-package-versions`](https://github.com/actions/delete-package-versions).

## Dockerfiles

Each service owns a Dockerfile at
`services/<service>/docker/Dockerfile`. The canonical shape lives at
[`templates/service-template/docker/Dockerfile`](../templates/service-template/docker/Dockerfile).

Images use a **single runtime stage** (`eclipse-temurin:8-jre-alpine`)
that `COPY`s a pre-built Spring Boot fat jar from the build context:

- Non-root `ftgo` user.
- `tini` as PID 1 for correct signal propagation.
- `curl`-based `HEALTHCHECK` against `/actuator/health`.
- Standard OCI labels (`title`, `description`, `source`, `version`,
  `revision`, `created`, `licenses`).

Producing the fat jar is deliberately **not** done inside the container.
The repo's Gradle wrapper is pinned to an older distribution whose
download requires TLS cipher suites that JDK 8 in
`eclipse-temurin:8-jdk-alpine` no longer negotiates, and running a full
Gradle build inside the image would force us to pull the entire repo
tree in just to run one task. Instead, the jar is produced by the CI
workflow (or the developer's host JDK) and only packaged here.

The build context **must** be the repository root so the `JAR_PATH`
build arg resolves. Every image is expected to stay under **200 MB**.

## Build arguments

| Arg              | Default                                                    | Purpose                                   |
|------------------|------------------------------------------------------------|-------------------------------------------|
| `SERVICE_NAME`   | `<service>`                                                | Used in labels and env vars.              |
| `SERVICE_VERSION`| `0.0.0-SNAPSHOT`                                           | Stamped into the `version` OCI label.     |
| `GIT_SHA`        | `unknown`                                                  | Stamped into the `revision` OCI label.    |
| `BUILD_DATE`     | `unknown`                                                  | Stamped into the `created` OCI label.     |
| `JAR_PATH`       | `services/<service>/build/libs/app.jar`                    | Relative path (from repo root) to the pre-built fat jar. |

## Tagging strategy

The CI workflow at
[`.github/workflows/docker-build.yml`](../.github/workflows/docker-build.yml)
uses [`docker/metadata-action`](https://github.com/docker/metadata-action)
to generate a consistent tag set for every push:

| Tag                           | Emitted on                   | Example                                       |
|-------------------------------|------------------------------|-----------------------------------------------|
| `<branch>`                    | Any branch push              | `feat-microservices-migration`                |
| `pr-<n>`                      | Pull requests                | `pr-42`                                       |
| `sha-<short>`                 | Every commit                 | `sha-1177bb9`                                 |
| `<version>`                   | Semver tags (`v1.2.3`)       | `1.2.3`                                       |
| `<major>.<minor>`             | Semver tags                  | `1.2`                                         |
| `<version>-<short-sha>`       | Every build                  | `0.0.0-SNAPSHOT-1177bb9`                      |
| `latest`                      | Pushes to default branch     | `latest`                                      |

The `<version>-<short-sha>` tag is the authoritative "this exact bits"
pointer; human-readable tags (`latest`, branch names) are moving
pointers.

## Scaffold gating (strangler-fig)

Per-service matrix entries are **gated** on the presence of a real
Spring Boot app. Before any Docker work runs, the workflow inspects
`services/<service>/build.gradle` for the `org.springframework.boot`
plugin. When the plugin is not applied, every downstream step (Gradle
build, image build, scan, push) is skipped and a summary is appended to
the GitHub Actions run. Note that a placeholder `@SpringBootApplication`
entrypoint under `src/main` alone does **not** trigger a build — the
scaffolds ship such placeholders without wiring in the plugin, so the
plugin itself is the authoritative signal. This keeps the workflow
green while the scaffolds from EM-30 are incrementally wired up in
later tickets.

## Vulnerability scanning

Every build is scanned with [Trivy](https://github.com/aquasecurity/trivy)
in two passes:

1. **SARIF report** covering `HIGH` and `CRITICAL` issues, uploaded to
   the GitHub Security tab via `github/codeql-action/upload-sarif`.
2. **Blocking scan** that fails the build on any fixable `CRITICAL`
   finding (`exit-code: '1'`, `ignore-unfixed: true`).

Scans run against the locally-loaded image *before* the push step, so a
vulnerable image never reaches the registry.

## Image size guardrail

The workflow inspects each built image with `docker image inspect` and
fails the job if the uncompressed size exceeds **200 MB**. If you
change the base image or add runtime dependencies, verify the budget is
still met.

## Retention policy

Untagged (dangling) image versions older than the 10 most recent are
pruned by the `cleanup` job after every push to the default branch.
Tagged releases are retained indefinitely — delete them manually via the
Packages UI if a CVE forces a takedown.

## Local development

Both flows assume you've already produced the fat jar on your host JDK:

```bash
./gradlew :services:consumer-service:bootJar -x test
cp services/consumer-service/build/libs/*.jar \
   services/consumer-service/build/libs/app.jar
```

- **Source-build loop** (builds images locally, no registry):

  ```bash
  docker compose -f docker-compose.dev.yml up --build
  ```

- **Registry images** (production-like):

  ```bash
  FTGO_IMAGE_TAG=feat-microservices-migration docker compose up
  ```

Host ports are offset to avoid collisions with the legacy
`ftgo-application` service on 8081:

| Service             | Host port |
|---------------------|-----------|
| `ftgo-application`  | `8081`    |
| `consumer-service`  | `8082`    |
| `order-service`     | `8083`    |
| `restaurant-service`| `8084`    |
| `courier-service`   | `8085`    |

## Triggering a build manually

```
gh workflow run docker-build.yml --ref feat/microservices-migration
```

Or open the Actions tab and use the **Run workflow** button.
