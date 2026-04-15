# FTGO Kubernetes Deployment

This directory contains Kubernetes manifests for deploying the FTGO microservices
platform using [Kustomize](https://kustomize.io/) overlays and
[ArgoCD](https://argo-cd.readthedocs.io/) for GitOps-based continuous delivery.

## Architecture

```
deployment/kubernetes/
├── base/                          # Shared base manifests
│   ├── postgres/                  # PostgreSQL StatefulSet + Service
│   ├── consumer-service/          # Consumer Service Deployment + HPA
│   ├── order-service/             # Order Service Deployment + HPA
│   ├── restaurant-service/        # Restaurant Service Deployment + HPA
│   └── courier-service/           # Courier Service Deployment + HPA
├── overlays/
│   ├── dev/                       # Dev environment (ftgo-dev namespace)
│   ├── staging/                   # Staging environment (ftgo-staging namespace)
│   └── prod/                      # Production environment (ftgo-prod namespace)
├── argocd/                        # ArgoCD Application CRs
├── stateful-services/             # Legacy MySQL manifests (deprecated)
├── scripts/                       # Legacy deployment scripts
└── misc/                          # Miscellaneous utilities
```

## Namespace Strategy

| Environment | Namespace      | Auto-deploy | Approval Required |
|------------|----------------|-------------|-------------------|
| Dev        | `ftgo-dev`     | Yes (on merge to main) | No |
| Staging    | `ftgo-staging` | No          | Yes (GitHub Environment gate) |
| Production | `ftgo-prod`    | No          | Yes (GitHub Environment gate) |

## Quick Start

### Prerequisites

- `kubectl` configured with cluster access
- `kustomize` v5.4+ installed
- ArgoCD installed in the cluster (optional, for GitOps)

### Deploy to dev (manual)

```bash
# Preview the rendered manifests
kustomize build deployment/kubernetes/overlays/dev

# Apply to cluster
kustomize build deployment/kubernetes/overlays/dev | kubectl apply -f -
```

### Deploy with ArgoCD

```bash
# Install the ArgoCD project and applications
kubectl apply -f deployment/kubernetes/argocd/appproject.yaml -n argocd
kubectl apply -f deployment/kubernetes/argocd/application-dev.yaml -n argocd
kubectl apply -f deployment/kubernetes/argocd/application-staging.yaml -n argocd
kubectl apply -f deployment/kubernetes/argocd/application-prod.yaml -n argocd
```

Dev auto-syncs on changes. Staging and production require manual sync via the
ArgoCD UI or CLI.

## Environment Promotion

The promotion flow is: **dev → staging → prod**.

### Via GitHub Actions

1. **Dev**: Automatically deployed on merge to `main` or `feat/microservices-migration`
2. **Staging**: Use the "CD: Promote Environment" workflow with `source=dev`
3. **Production**: Use the "CD: Promote Environment" workflow with `source=staging`

Staging and production deployments require approval through GitHub Environments.
Configure required reviewers in **Settings → Environments** for `staging` and
`production`.

### Via ArgoCD

1. Dev auto-syncs
2. For staging/prod, run `argocd app sync ftgo-staging` or `argocd app sync ftgo-prod`
   after verifying the previous environment

## Secrets Management

**No secrets are hardcoded in manifests.** Database credentials are managed via:

1. **Dev**: Kustomize `secretGenerator` with placeholder values (safe for development)
2. **Staging/Prod**: Use [Bitnami Sealed Secrets](https://github.com/bitnami-labs/sealed-secrets)
   or [External Secrets Operator](https://external-secrets.io/)

### Sealing secrets for staging/prod

```bash
# Create a plain secret YAML
kubectl create secret generic ftgo-db-credentials \
  --from-literal=postgres-user=ftgo \
  --from-literal=postgres-password=<REAL_PASSWORD> \
  --dry-run=client -o yaml > /tmp/secret.yaml

# Seal it with the cluster's public key
kubeseal --format yaml --cert <pub-cert.pem> < /tmp/secret.yaml > sealed-secret.yaml

# Clean up the plaintext
rm /tmp/secret.yaml
```

## Rolling Updates and Zero-Downtime Deployment

All service Deployments are configured for zero-downtime rolling updates:

- **`maxUnavailable: 0`** — no pods are terminated before new ones are ready
- **`maxSurge: 1`** — one extra pod is created during rollout
- **Startup probes** — prevent traffic before the JVM is ready (up to 100s)
- **Readiness probes** — gate traffic to healthy pods only
- **Liveness probes** — restart unresponsive pods
- **`preStop` hook** — 5-second sleep allows in-flight requests to drain
- **PodDisruptionBudgets** (prod only) — ensure minimum availability during
  node maintenance

## Resource Scaling

| Environment | Replicas | CPU Request | Memory Request | HPA Max |
|------------|----------|-------------|----------------|---------|
| Dev        | 1        | 100m        | 128Mi          | 3       |
| Staging    | 2        | 200m        | 256Mi          | 5       |
| Production | 3        | 500m        | 512Mi          | 20      |

HPA scales on CPU (70% target) and memory (80% target) utilization.

## Migration from Legacy MySQL

The legacy `stateful-services/ftgo-mysql-deployment.yml` used:
- Deprecated `apps/v1beta1` API version
- Hardcoded MySQL credentials in plaintext
- No health checks, no resource limits

The new setup replaces this with:
- `apps/v1` StatefulSet for PostgreSQL 16
- Credentials via Kubernetes Secrets (sealed-secrets ready)
- Readiness and liveness probes
- Resource requests and limits
- Multi-database initialization via ConfigMap-mounted init script
