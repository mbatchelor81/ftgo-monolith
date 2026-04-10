# FTGO Kubernetes Deployment Guide

## Overview

This document describes the Kubernetes deployment architecture for the FTGO microservices platform. The deployment uses **Kustomize** for environment-specific configuration management and **GitHub Actions** for automated CI/CD pipelines.

## Architecture

### Services

| Service | Description | Port |
|---------|-------------|------|
| `ftgo-order-service` | Order lifecycle management | 8080 |
| `ftgo-consumer-service` | Consumer registration and validation | 8080 |
| `ftgo-courier-service` | Courier management and scheduling | 8080 |
| `ftgo-restaurant-service` | Restaurant and menu management | 8080 |
| `ftgo-mysql` | MySQL 8.0 database (StatefulSet) | 3306 |

### Namespace Strategy

| Environment | Namespace | Purpose |
|-------------|-----------|---------|
| Development | `ftgo-dev` | Feature testing and integration |
| Staging | `ftgo-staging` | Pre-production validation |
| Production | `ftgo-prod` | Live traffic |

## Directory Structure

```
k8s/
├── base/                              # Base manifests (shared across environments)
│   ├── kustomization.yaml
│   ├── namespace.yaml
│   ├── mysql/
│   │   ├── kustomization.yaml
│   │   ├── statefulset.yaml
│   │   ├── service.yaml
│   │   ├── configmap.yaml
│   │   └── secret.yaml
│   ├── ftgo-order-service/
│   │   ├── kustomization.yaml
│   │   ├── deployment.yaml
│   │   ├── service.yaml
│   │   ├── configmap.yaml
│   │   ├── secret.yaml
│   │   └── hpa.yaml
│   ├── ftgo-consumer-service/
│   │   └── ... (same structure)
│   ├── ftgo-courier-service/
│   │   └── ... (same structure)
│   └── ftgo-restaurant-service/
│       └── ... (same structure)
├── overlays/
│   ├── dev/
│   │   ├── kustomization.yaml
│   │   └── namespace.yaml
│   ├── staging/
│   │   ├── kustomization.yaml
│   │   └── namespace.yaml
│   └── prod/
│       ├── kustomization.yaml
│       └── namespace.yaml
```

## Deploying

### Prerequisites

- `kubectl` v1.28+ configured with cluster access
- `kustomize` v5+ (or use `kubectl -k`)
- Docker registry access (GHCR)

### Deploy to an Environment

```bash
# Deploy to dev
kustomize build k8s/overlays/dev | kubectl apply -f -

# Deploy to staging
kustomize build k8s/overlays/staging | kubectl apply -f -

# Deploy to production
kustomize build k8s/overlays/prod | kubectl apply -f -
```

### Validate Manifests (Dry Run)

```bash
# Preview generated manifests without applying
kustomize build k8s/overlays/dev

# Server-side dry run
kustomize build k8s/overlays/dev | kubectl apply --dry-run=server -f -
```

### Check Deployment Status

```bash
# Check all pods in an environment
kubectl get pods -n ftgo-dev

# Check rollout status for a specific service
kubectl rollout status deployment/ftgo-order-service -n ftgo-dev

# View service endpoints
kubectl get endpoints -n ftgo-dev

# Check HPA status
kubectl get hpa -n ftgo-dev
```

## Environment Configuration

### Dev Environment

- **Replicas**: 1 per service
- **HPA**: min=1, max=2
- **Resources**: 128Mi-256Mi memory, 100m-250m CPU
- **MySQL Storage**: 2Gi
- **Image Tag**: `dev-latest`

### Staging Environment

- **Replicas**: 2 per service
- **HPA**: min=2, max=4
- **Resources**: 256Mi-512Mi memory, 250m-500m CPU
- **MySQL Storage**: 10Gi
- **Image Tag**: `staging-latest`

### Production Environment

- **Replicas**: 3 per service
- **HPA**: min=3, max=10
- **Resources**: 512Mi-1Gi memory, 500m-1000m CPU
- **MySQL Storage**: 50Gi
- **Image Tag**: `prod-latest`
- **Pod Anti-Affinity**: Pods spread across nodes

## CI/CD Pipelines

### Automated Deployment (`cd-deploy.yml`)

Triggers automatically on merge to `main` when files change in `services/` or `k8s/`.

**Pipeline stages:**
1. **Build**: Builds and pushes Docker images for all 4 microservices to GHCR
2. **Validate**: Validates all Kustomize overlays (dev, staging, prod)
3. **Deploy to Dev**: Automatically deploys to `ftgo-dev` namespace
4. **Health Verification**: Checks rollout status and service health

Can also be triggered manually via `workflow_dispatch` for any environment.

### Environment Promotion (`cd-promote.yml`)

Manual workflow for promoting a specific image tag between environments.

**Allowed promotion paths:**
- `dev` → `staging` (requires staging environment approval)
- `staging` → `prod` (requires production environment approval)

**Usage:**
1. Go to Actions → "CD - Promote Environment"
2. Select source and target environments
3. Enter the image tag to promote
4. Optionally enable dry-run mode
5. Click "Run workflow"
6. Approve the deployment when prompted (staging/prod require approval gates)

### Setting Up Approval Gates

Configure GitHub Environment protection rules:

1. Go to **Settings** → **Environments**
2. Create environments: `dev`, `staging`, `prod`
3. For `staging`: Add required reviewers
4. For `prod`: Add required reviewers + deployment branch restrictions (main only)

## Secrets Management

### Strategy

Secrets are **never** hardcoded in manifests. The deployment uses a layered approach:

| Environment | Secret Management |
|-------------|-------------------|
| Development | Kustomize `secretGenerator` with dev-only values |
| Staging | External-secrets operator or sealed-secrets (placeholder values in overlays) |
| Production | External-secrets operator or sealed-secrets (placeholder values in overlays) |

### Required Secrets

| Secret | Keys | Used By |
|--------|------|---------|
| `ftgo-mysql-secret` | `MYSQL_ROOT_PASSWORD`, `MYSQL_USER`, `MYSQL_PASSWORD` | MySQL StatefulSet |
| `ftgo-{service}-secret` | `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Each microservice |

### GitHub Actions Secrets

| Secret | Description |
|--------|-------------|
| `KUBE_CONFIG_DEV` | Base64-encoded kubeconfig for dev cluster |
| `KUBE_CONFIG_STAGING` | Base64-encoded kubeconfig for staging cluster |
| `KUBE_CONFIG_PROD` | Base64-encoded kubeconfig for prod cluster |

### Setting Up External Secrets (Recommended for Staging/Prod)

1. Install the External Secrets Operator:
   ```bash
   helm repo add external-secrets https://charts.external-secrets.io
   helm install external-secrets external-secrets/external-secrets -n external-secrets --create-namespace
   ```

2. Configure a `SecretStore` pointing to your secrets backend (AWS Secrets Manager, HashiCorp Vault, etc.)

3. Replace the `secretGenerator` entries in staging/prod overlays with `ExternalSecret` resources.

## Health Checks

All microservices expose Spring Boot Actuator health endpoints:

| Probe | Path | Purpose |
|-------|------|---------|
| Startup | `/actuator/health` | Wait for app to start (up to 150s) |
| Liveness | `/actuator/health/liveness` | Restart if unhealthy |
| Readiness | `/actuator/health/readiness` | Remove from service if not ready |

MySQL uses `mysqladmin ping` for both liveness and readiness probes.

## Rolling Updates

All Deployments are configured with a zero-downtime rolling update strategy:

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1        # Create 1 new pod before removing old ones
    maxUnavailable: 0  # Never remove old pods until new ones are ready
```

Combined with:
- **`preStop` hook**: 10-second sleep to allow load balancer to drain connections
- **`terminationGracePeriodSeconds`**: 30 seconds for graceful shutdown
- **Readiness probes**: New pods only receive traffic when fully ready

## Rollback

### Automatic Rollback

If a deployment fails health checks, Kubernetes will automatically stop the rollout.

### Manual Rollback

```bash
# View rollout history
kubectl rollout history deployment/ftgo-order-service -n ftgo-dev

# Rollback to previous revision
kubectl rollout undo deployment/ftgo-order-service -n ftgo-dev

# Rollback to specific revision
kubectl rollout undo deployment/ftgo-order-service -n ftgo-dev --to-revision=2
```

## Troubleshooting

### Common Issues

**Pods stuck in Pending:**
```bash
kubectl describe pod <pod-name> -n ftgo-dev
# Check for resource constraints or node scheduling issues
```

**CrashLoopBackOff:**
```bash
kubectl logs <pod-name> -n ftgo-dev --previous
# Check application startup errors
```

**Database connection failures:**
```bash
# Verify MySQL is running
kubectl get pods -l app.kubernetes.io/name=ftgo-mysql -n ftgo-dev

# Check MySQL logs
kubectl logs statefulset/ftgo-mysql -n ftgo-dev

# Verify service DNS resolution
kubectl run -it --rm debug --image=busybox -n ftgo-dev -- nslookup ftgo-mysql
```
