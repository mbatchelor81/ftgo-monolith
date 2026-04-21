# FTGO Container Images

This document describes how FTGO microservice container images are built,
tagged, scanned, and distributed.

## Registry

All service images are published to **GitHub Container Registry (GHCR)**
under the org namespace:

```
ghcr.io/mbatchelor81/ftgo-<service>:<tag>
```

where `<service>` is one of `consumer-service`, `order-service`,
`restaurant-service`, or `courier-service`.

GHCR was chosen because:

- It is free for public images and generously priced for private ones.
- Authentication uses the built-in `GITHUB_TOKEN` on Actions — no extra
  secrets to rotate.
- Package retention policies can be enforced via
  [`actions/delete-package-versions`](https://github.com/actions/delete-package-versions).

## Dockerfiles

Each service owns a Dockerfile at
`services/<service>/docker/Dockerfile`. The canonical shape lives at
[`templates/service-template/docker/Dockerfile`](../templates/service-template/docker/Dockerfile)
and follows a **two-stage** pattern:

1. `builder` — `eclipse-temurin:8-jdk-alpine` running
   `./gradlew :services:<service>:bootJar -x test`.
2. `runtime` — `eclipse-temurin:8-jre-alpine` with a non-root `ftgo` user,
   `tini` as PID 1, a curl-based `HEALTHCHECK` against
   `/actuator/health`, and the fat jar copied from the builder.

The build context **must** be the repository root because Gradle needs
access to `settings.gradle`, `buildSrc/`, and the shared `libs/` and
`ftgo-*` modules. Every image is expected to stay under **200 MB**.

## Build arguments

| Arg              | Default             | Purpose                                   |
|------------------|---------------------|-------------------------------------------|
| `SERVICE_NAME`   | `<service>`         | Gradle module under `services/` to build. |
| `SERVICE_VERSION`| `0.0.0-SNAPSHOT`    | Stamped into the `version` OCI label.     |
| `GIT_SHA`        | `unknown`           | Stamped into the `revision` OCI label.    |
| `BUILD_DATE`     | `unknown`           | Stamped into the `created` OCI label.     |

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
fails the job if the compressed layer size exceeds **200 MB**. If you
change the base image or add runtime dependencies, verify the budget is
still met.

## Retention policy

Untagged (dangling) image versions older than the 10 most recent are
pruned by the `cleanup` job after every push to the default branch.
Tagged releases are retained indefinitely — delete them manually via the
Packages UI if a CVE forces a takedown.

## Local development

- **Source-build loop** (no registry):

  ```bash
  docker compose -f docker-compose.dev.yml up --build
  ```

- **Registry images** (production-like):

  ```bash
  FTGO_IMAGE_TAG=feat-microservices-migration docker compose up
  ```

Host ports are offset to avoid collisions with the legacy
`ftgo-application` service on 8081:

| Service            | Host port |
|--------------------|-----------|
| `ftgo-application` | `8081`    |
| `consumer-service` | `8082`    |
| `order-service`    | `8083`    |
| `restaurant-service`| `8084`   |
| `courier-service`  | `8085`    |

## Triggering a build manually

```
gh workflow run docker-build.yml --ref feat/microservices-migration
```

Or open the Actions tab and use the **Run workflow** button.
