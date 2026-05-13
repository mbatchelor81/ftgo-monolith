# Container Strategy

> FTGO Platform — Container Registry, Image Build, and Distribution

## Container Registry

**Registry**: GitHub Container Registry (GHCR) at `ghcr.io`

**Why GHCR?**
- Native integration with GitHub Actions (no extra credentials)
- Supports OCI-compliant images
- Granular access control via GitHub permissions
- Free for public packages, included with GitHub plans for private

**Image naming convention**:
```
ghcr.io/mbatchelor81/ftgo-<service-name>:<tag>
```

Examples:
- `ghcr.io/mbatchelor81/ftgo-consumer-service:latest`
- `ghcr.io/mbatchelor81/ftgo-order-service:a1b2c3d`
- `ghcr.io/mbatchelor81/ftgo-restaurant-service:1.0.0`

## Image Tagging Strategy

Each image receives multiple tags for different use cases:

| Tag Format | Example | Purpose |
|------------|---------|---------|
| `latest` | `latest` | Most recent main branch build |
| Git SHA (short) | `a1b2c3d` | Exact commit traceability |
| Branch name | `main` | Current state of a branch |
| Semantic version | `1.0.0` | Release versions |
| Major.Minor | `1.0` | Floating minor version |

**Immutability**: SHA-based tags are immutable and should never be overwritten.
`latest` and branch tags are mutable floating tags.

## Dockerfile Design

### Multi-stage Build

All microservice Dockerfiles use a two-stage build:

```
┌─────────────────────────────┐
│ Stage 1: builder            │
│ eclipse-temurin:17-jdk-alpine│
│                             │
│ • Copy Gradle wrapper       │
│ • Copy build files          │
│ • Copy shared libraries     │
│ • Copy service source       │
│ • Run Gradle bootJar        │
└──────────┬──────────────────┘
           │ COPY *.jar
┌──────────▼──────────────────┐
│ Stage 2: runtime            │
│ eclipse-temurin:17-jre-alpine│
│                             │
│ • Non-root user (ftgo)      │
│ • Health check              │
│ • OCI labels                │
│ • ~200 MB final image       │
└─────────────────────────────┘
```

### Layer Caching Strategy

Dockerfiles are structured for optimal layer caching:
1. Gradle wrapper and build configuration (rarely changes)
2. Shared libraries (changes less frequently)
3. Service source code (changes most frequently)

### Security

- **Non-root execution**: All containers run as the `ftgo` user
- **Minimal base image**: Alpine-based JRE image (~80 MB base)
- **No build tools in runtime**: Multi-stage build excludes JDK, Gradle, source code
- **Health checks**: All containers expose `/actuator/health`

## CI/CD Pipeline

### Workflow: `ci-docker-build.yml`

**Trigger conditions**:
- **Push to `main`**: Builds and pushes all service images to GHCR
- **Pull requests**: Builds images (no push) to validate Dockerfiles

**Pipeline stages**:
1. **Change detection** — Identifies which services have changed
2. **Build matrix** — Parallel builds for each changed service
3. **Vulnerability scan** — Trivy scans for CRITICAL and HIGH CVEs
4. **Push** — Publishes to GHCR (main branch only)

```
PR opened ──► Change Detection ──► Build (no push) ──► Trivy Scan
                                                         │
Merge to main ──► Build all ──► Push to GHCR ──► Trivy Scan ──► SARIF Upload
```

### Vulnerability Scanning

**Tool**: [Trivy](https://github.com/aquasecurity/trivy)

- Scans for CRITICAL and HIGH severity vulnerabilities
- Results uploaded as SARIF to GitHub Security tab
- Non-blocking (exit-code: 0) to avoid breaking builds for upstream CVEs
- Review findings in **Security → Code scanning alerts**

## Local Development

### Docker Compose

```bash
# Start all services
docker-compose -f infrastructure/docker/docker-compose.services.yml up --build

# Start specific services
docker-compose -f infrastructure/docker/docker-compose.services.yml up mysql consumer-service

# Rebuild after code changes
docker-compose -f infrastructure/docker/docker-compose.services.yml up --build order-service
```

### Service Ports

| Service | Host Port | Container Port |
|---------|-----------|----------------|
| MySQL | 3306 | 3306 |
| Consumer Service | 8081 | 8080 |
| Order Service | 8082 | 8080 |
| Restaurant Service | 8083 | 8080 |
| Courier Service | 8084 | 8080 |

### Environment Configuration

Copy the example env file:
```bash
cp infrastructure/docker/.env.example infrastructure/docker/.env
```

### Combined with Monitoring

```bash
# Run services + monitoring stack
docker-compose \
  -f infrastructure/docker/docker-compose.services.yml \
  -f infrastructure/monitoring/docker-compose.monitoring.yml \
  up --build
```

## Image Cleanup & Retention

### GHCR Retention Policy

Configure retention via GitHub repository settings or the
[`ghcr-cleanup-action`](https://github.com/dataaxiom/ghcr-cleanup-action):

| Tag Type | Retention |
|----------|-----------|
| `latest` | Always kept |
| Semantic version (`v*`) | Kept indefinitely |
| Branch tags | 30 days after branch deletion |
| SHA tags | 90 days |
| Untagged images | 7 days |

### Recommended: Add a scheduled cleanup workflow

```yaml
# .github/workflows/cleanup-images.yml (not included — add when needed)
name: Cleanup old container images
on:
  schedule:
    - cron: "0 3 * * 0"  # Weekly on Sunday at 3 AM
```

## Jib Integration (Alternative)

The `build-logic/` convention plugins include `ftgo.docker-conventions.gradle`
which configures Google Jib for building OCI images without Docker. This is
available as an alternative to Dockerfile-based builds:

```bash
./gradlew :consumer-service-app:jibDockerBuild
```

Jib builds are faster for development but Dockerfiles provide more control
for production image customization.

## Future Considerations

1. **Multi-architecture builds**: Add `linux/arm64` platform support via
   Docker Buildx for Apple Silicon developers
2. **Base image consolidation**: Create a shared FTGO base image with common
   runtime dependencies
3. **Image signing**: Implement Cosign for supply chain security
4. **Helm charts**: Package Docker Compose services as Helm charts for
   Kubernetes deployment
