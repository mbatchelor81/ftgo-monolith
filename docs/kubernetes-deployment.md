# Kubernetes Deployment Guide

## Overview

FTGO services are deployed to Kubernetes using **Kustomize** for environment-specific
configuration. The deployment structure supports three environments — **dev**, **staging**,
and **prod** — each in its own namespace with tailored resource limits, replica counts,
and secrets.

## Architecture

```
deployment/kubernetes/
├── base/                        # Shared manifests (all environments)
│   ├── kustomization.yaml       # Aggregates all base resources
│   ├── namespace.yaml           # Base namespace definition
│   ├── mysql/                   # MySQL StatefulSet (apps/v1)
│   │   ├── statefulset.yaml
│   │   ├── service.yaml
│   │   ├── configmap.yaml
│   │   ├── init-configmap.yaml
│   │   └── secret.yaml
│   ├── consumer-service/        # Consumer Service
│   │   ├── deployment.yaml
│   │   ├── service.yaml
│   │   ├── configmap.yaml
│   │   └── hpa.yaml
│   ├── order-service/           # Order Service
│   ├── restaurant-service/      # Restaurant Service
│   └── courier-service/         # Courier Service
├── overlays/
│   ├── dev/                     # ftgo-dev namespace
│   │   ├── kustomization.yaml
│   │   ├── namespace.yaml
│   │   └── patches/
│   ├── staging/                 # ftgo-staging namespace
│   │   ├── kustomization.yaml
│   │   ├── namespace.yaml
│   │   └── patches/
│   └── prod/                    # ftgo-prod namespace
│       ├── kustomization.yaml
│       ├── namespace.yaml
│       ├── pdb-*.yaml           # PodDisruptionBudgets
│       └── patches/
```

## Services

| Service              | Image                                          | Port |
|----------------------|------------------------------------------------|------|
| consumer-service     | `ghcr.io/mbatchelor81/ftgo-consumer-service`   | 8080 |
| order-service        | `ghcr.io/mbatchelor81/ftgo-order-service`       | 8080 |
| restaurant-service   | `ghcr.io/mbatchelor81/ftgo-restaurant-service` | 8080 |
| courier-service      | `ghcr.io/mbatchelor81/ftgo-courier-service`     | 8080 |
| mysql                | `mysql:8.0`                                     | 3306 |

## Environments

### Namespace Strategy

| Environment | Namespace      | Purpose                            |
|-------------|----------------|-------------------------------------|
| dev         | `ftgo-dev`     | Development and feature testing     |
| staging     | `ftgo-staging` | Pre-production validation           |
| prod        | `ftgo-prod`    | Production workloads                |

### Resource Allocation

| Environment | Service Replicas | Service Memory | MySQL Storage |
|-------------|-----------------|----------------|---------------|
| dev         | 1               | 192–384 Mi     | 2 Gi          |
| staging     | 2               | 256–512 Mi     | 10 Gi         |
| prod        | 2–3             | 384 Mi–1 Gi    | 50 Gi         |

### HPA Configuration

- **dev**: min 1, max 2 (cost savings)
- **staging**: base defaults (min 1, max 5–10)
- **prod**: base defaults with higher resource headroom

## Deploying

### Prerequisites

- `kubectl` configured with cluster credentials
- `kustomize` CLI (v4+) or `kubectl` with built-in kustomize support

### Manual Deployment

```bash
# Preview manifests
kustomize build deployment/kubernetes/overlays/dev

# Apply to cluster
kustomize build deployment/kubernetes/overlays/dev | kubectl apply -f -

# Or using kubectl directly
kubectl apply -k deployment/kubernetes/overlays/dev
```

### Deploy a Specific Image Tag

Edit the overlay's `kustomization.yaml` images section, or use:

```bash
cd deployment/kubernetes/overlays/dev
kustomize edit set image ghcr.io/mbatchelor81/ftgo-order-service:sha-abc1234
kustomize build . | kubectl apply -f -
```

### GitHub Actions CD

The `cd-deploy.yml` workflow supports:

1. **Automatic**: Deploys to `dev` on push to `main` (when K8s or service files change)
2. **Manual**: Use `workflow_dispatch` to deploy any environment with a specific image tag
3. **Selective**: Deploy specific services via the `services` input

```
Actions → CD — Deploy to Kubernetes → Run workflow
  Environment: staging
  Image Tag: sha-abc1234
  Services: order-service,consumer-service
```

## Environment Promotion

Promotion follows the pipeline: **dev → staging → prod**.

### Using the Promotion Workflow

```
Actions → CD — Promote Environment → Run workflow
  Source Environment: dev        (promotes TO staging)
  Image Tag: sha-abc1234
```

### Promotion Checklist

Before promoting to production:

- [ ] All integration tests pass in staging
- [ ] Performance baseline meets SLA targets
- [ ] Database migrations (Flyway) are compatible
- [ ] Rollback plan documented

## Secrets Management

Base secrets use placeholder values. Each overlay replaces them via Kustomize
`secretGenerator` with `behavior: replace`.

**For production**, replace the literal values in the overlay with references to an
external secret manager:

```yaml
# Option 1: Use SealedSecrets or ExternalSecrets operator
# Option 2: Inject via CI/CD pipeline environment variables
# Option 3: Use a CSI secret store driver
```

**Never commit real credentials.** The placeholder values in overlays are for
demonstration only.

## Health Checks

All services expose Spring Boot Actuator health endpoints:

| Probe     | Path                          | Purpose                  |
|-----------|-------------------------------|--------------------------|
| Liveness  | `/actuator/health/liveness`   | Restart unhealthy pods   |
| Readiness | `/actuator/health/readiness`  | Gate traffic routing     |

MySQL uses `mysqladmin ping` for both probes.

## Troubleshooting

### Check pod status

```bash
kubectl -n ftgo-dev get pods
kubectl -n ftgo-dev describe pod <pod-name>
kubectl -n ftgo-dev logs <pod-name>
```

### Restart a deployment

```bash
kubectl -n ftgo-dev rollout restart deployment/ftgo-order-service
```

### Rollback

```bash
kubectl -n ftgo-dev rollout undo deployment/ftgo-order-service
```

### Check HPA status

```bash
kubectl -n ftgo-dev get hpa
kubectl -n ftgo-dev describe hpa ftgo-order-service
```
