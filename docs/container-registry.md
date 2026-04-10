# Container Registry & Docker Image Build Automation

> **Jira**: EM-34 — Phase 2: CI/CD Pipeline for Microservices

---

## Overview

This document describes the container registry setup, image build automation, tagging strategy, vulnerability scanning, and local development workflow for the FTGO microservices.

---

## Container Registry

We use **GitHub Container Registry (GHCR)** as the container registry.

| Property         | Value                                                    |
|------------------|----------------------------------------------------------|
| Registry         | `ghcr.io`                                                |
| Image prefix     | `ghcr.io/mbatchelor81/<service-name>`                    |
| Authentication   | `GITHUB_TOKEN` (automatic in GitHub Actions)             |
| Visibility       | Inherits from repository settings                        |

### Available Images

| Service              | Image                                                        |
|----------------------|--------------------------------------------------------------|
| Order Service        | `ghcr.io/mbatchelor81/ftgo-order-service`                    |
| Consumer Service     | `ghcr.io/mbatchelor81/ftgo-consumer-service`                 |
| Restaurant Service   | `ghcr.io/mbatchelor81/ftgo-restaurant-service`               |
| Courier Service      | `ghcr.io/mbatchelor81/ftgo-courier-service`                  |

---

## Image Tagging Strategy

Every image build produces multiple tags for flexibility:

| Tag Format                        | Example                                     | Purpose                        |
|-----------------------------------|---------------------------------------------|--------------------------------|
| `<version>-<git-sha>`            | `0.1.0-abc1234`                             | **Primary**: unique per build  |
| `<version>`                       | `0.1.0`                                     | Latest build of that version   |
| `sha-<git-sha>`                  | `sha-abc1234`                               | Lookup by commit               |
| `latest`                          | `latest`                                    | Most recent main build         |

- **Version** is read from `gradle.properties` (defaults to `0.1.0`).
- **Git SHA** is the short (7-char) commit hash.
- The `latest` tag is only applied on pushes to `main`.

---

## CI Workflow

The workflow is defined in `.github/workflows/docker-build-push.yml`.

### Trigger Conditions

| Event       | Branch | Condition                                                |
|-------------|--------|----------------------------------------------------------|
| `push`      | `main` | Changes in `services/`, `shared/`, `buildSrc/`, or root build files |
| `pull_request` | `main` | Same path filters (build only, no push)               |

### Pipeline Stages

```
detect-changes → build-and-push (matrix) → cleanup
```

1. **Detect Changes**: Identifies which services have changed. If shared libraries or build config changed, all services are rebuilt.
2. **Build & Push** (per service, parallel):
   - Builds multi-stage Docker image
   - Pushes to GHCR (only on `push` to `main`)
   - Runs Trivy vulnerability scan
   - Uploads SARIF results to GitHub Security tab
3. **Cleanup**: Removes old untagged image versions, keeping the last 10.

### Caching

Docker layer caching uses GitHub Actions cache (`type=gha`) scoped per service for fast incremental builds.

---

## Vulnerability Scanning

[Trivy](https://github.com/aquasecurity/trivy) scans every built image for known CVEs.

| Setting       | Value                   |
|---------------|-------------------------|
| Scanner       | Trivy v0.28.0           |
| Severities    | CRITICAL, HIGH          |
| Output        | SARIF → GitHub Security |
| Exit code     | `0` (non-blocking)      |

Results are visible in the repository's **Security → Code scanning alerts** tab.

---

## Image Cleanup / Retention Policy

The CI workflow includes an automated cleanup job:

- **Runs after**: Successful image push to GHCR
- **Policy**: Keep last **10 tagged versions** per service
- **Scope**: Deletes only **untagged** versions
- **Implementation**: `actions/delete-package-versions@v5`

---

## Multi-Stage Dockerfile Design

Each service uses an identical two-stage build pattern:

### Stage 1 — Builder (`eclipse-temurin:17-jdk`)
- Copies Gradle wrapper, build config, buildSrc, and shared libraries
- Copies the service source code
- Runs `./gradlew :services:<service>:bootJar --no-daemon -x test`

### Stage 2 — Runtime (`eclipse-temurin:17-jre`)
- Installs `curl` (health checks) and `dumb-init` (signal handling)
- Creates non-root user `ftgo` (UID 1001)
- Copies the built JAR from the builder stage
- Exposes port `8080`
- Health check via Spring Boot Actuator (`/actuator/health`)
- Uses `dumb-init` as PID 1 for proper SIGTERM handling
- JVM flags: container-aware memory (`-XX:MaxRAMPercentage=75.0`)

### Security Features
- **Non-root user**: Runs as `ftgo` (UID 1001, GID 1001)
- **Minimal base**: JRE-only runtime (no JDK, no build tools)
- **Signal handling**: `dumb-init` ensures graceful shutdown
- **No secrets in image**: All config via environment variables

---

## Local Development

### Using Docker Compose

```bash
# Start all services (builds locally if no registry image available)
docker compose -f docker-compose.dev.yml up -d

# Start only MySQL (for running services in IDE)
docker compose -f docker-compose.dev.yml up mysql -d

# Use registry images with a specific tag
FTGO_IMAGE_TAG=0.1.0-abc1234 docker compose -f docker-compose.dev.yml up -d

# View logs
docker compose -f docker-compose.dev.yml logs -f ftgo-order-service

# Tear down
docker compose -f docker-compose.dev.yml down -v
```

### Service Ports

| Service              | Host Port | Container Port |
|----------------------|-----------|----------------|
| MySQL                | 3306      | 3306           |
| Order Service        | 8081      | 8080           |
| Consumer Service     | 8082      | 8080           |
| Restaurant Service   | 8083      | 8080           |
| Courier Service      | 8084      | 8080           |

### Building a Single Service Image Locally

```bash
# From the repository root
docker build -f services/ftgo-order-service/docker/Dockerfile -t ftgo-order-service:local .
```

### Pulling from GHCR

```bash
# Authenticate (one-time)
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Pull a specific version
docker pull ghcr.io/mbatchelor81/ftgo-order-service:0.1.0-abc1234
```

---

## Database Setup

Each service expects its own MySQL database. The `mysql/init/` directory can contain initialization scripts. For local development, create the databases manually or add an init script:

```sql
CREATE DATABASE IF NOT EXISTS ftgo_order_service;
CREATE DATABASE IF NOT EXISTS ftgo_consumer_service;
CREATE DATABASE IF NOT EXISTS ftgo_restaurant_service;
CREATE DATABASE IF NOT EXISTS ftgo_courier_service;

GRANT ALL PRIVILEGES ON ftgo_order_service.* TO 'mysqluser'@'%';
GRANT ALL PRIVILEGES ON ftgo_consumer_service.* TO 'mysqluser'@'%';
GRANT ALL PRIVILEGES ON ftgo_restaurant_service.* TO 'mysqluser'@'%';
GRANT ALL PRIVILEGES ON ftgo_courier_service.* TO 'mysqluser'@'%';
FLUSH PRIVILEGES;
```
