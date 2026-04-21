# Kubernetes deployment (EM-35)

This document describes how the FTGO platform is packaged and deployed to
Kubernetes across `dev`, `staging`, and `prod` environments.

## Layout

```
deployment/
├── kubernetes/stateful-services/   # MySQL StatefulSet + Secret + ConfigMap
└── kustomize/
    ├── base/                       # composes all services into one bundle
    └── overlays/
        ├── dev/                    # ftgo-dev namespace
        ├── staging/                # ftgo-staging namespace
        └── prod/                   # ftgo-prod namespace

services/
├── consumer-service/k8s/           # Deployment + Service + ConfigMap + HPA
├── order-service/k8s/
├── courier-service/k8s/
└── restaurant-service/k8s/

platform/shared-infrastructure/k8s/
├── namespaces/                     # ftgo-{dev,staging,prod}
├── network-policies/               # default-deny + DNS + intra-namespace allow
└── rbac/                           # ftgo-deployer ServiceAccount + Role

.github/workflows/
├── k8s-validate.yml                # PR validation (yaml + kustomize build + kubeconform)
└── deploy.yml                      # CD pipeline (dev → staging → prod)
```

Every manifest uses a current GA API version (no `apps/v1beta1`).

## Rendering

`kustomize build` is the single source of truth. Every overlay resolves to a
self-contained manifest bundle that can be `kubectl apply -f`'d or piped
directly to `kubectl apply -k`.

```bash
kubectl kustomize deployment/kustomize/overlays/dev
kubectl kustomize deployment/kustomize/overlays/staging
kubectl kustomize deployment/kustomize/overlays/prod
```

Differences between overlays:

| Knob                | dev   | staging | prod  |
| ------------------- | ----- | ------- | ----- |
| Namespace           | ftgo-dev | ftgo-staging | ftgo-prod |
| Image tag           | dev   | staging | stable |
| Deployment replicas | 1     | 2       | 3     |
| HPA min → max       | 1 → 3 | 2 → 6   | 3 → 20 |
| CPU request         | 50m   | 100m    | 250m  |
| Memory request      | 128Mi | 256Mi   | 512Mi |

## CD pipeline

The `deploy.yml` workflow triggers on:

- Pushes to `feat/microservices-migration`, `main`, or `master` that touch a
  Kubernetes path — promotes through all three environments sequentially.
- `workflow_dispatch` — manual runs can target a single environment and
  override the image tag.

Job graph: `validate` → `deploy-dev` → `deploy-staging` → `deploy-prod`.

Each environment job targets a GitHub Environment of the same name
(`ftgo-dev`, `ftgo-staging`, `ftgo-prod`). Required approvers on the
`ftgo-staging` and `ftgo-prod` environments act as promotion gates — the
workflow will pause before those jobs run until a reviewer approves.

### Required GitHub secrets

| Secret               | Scope (Environment) | Value                                            |
| -------------------- | ------------------- | ------------------------------------------------ |
| `KUBE_CONFIG_DEV`    | `ftgo-dev`          | Base64-encoded kubeconfig with `ftgo-deployer` permissions on the dev cluster/namespace |
| `KUBE_CONFIG_STAGING`| `ftgo-staging`      | Base64-encoded kubeconfig for staging            |
| `KUBE_CONFIG_PROD`   | `ftgo-prod`         | Base64-encoded kubeconfig for prod               |

Scope each kubeconfig to the `ftgo-deployer` ServiceAccount (see
`platform/shared-infrastructure/k8s/rbac/deployer.yaml`) so the pipeline can
only manage FTGO resources.

## Secrets management

The `ftgo-db-secret` checked into
`deployment/kubernetes/stateful-services/ftgo-db-secret.yml` is a **placeholder
for local/dev clusters only**. Staging and prod clusters MUST provision this
Secret via one of:

- [sealed-secrets](https://github.com/bitnami-labs/sealed-secrets): commit a
  `SealedSecret` manifest to the repo; the controller decrypts it in-cluster.
- [external-secrets](https://external-secrets.io/): reference a value stored
  in AWS Secrets Manager, HashiCorp Vault, GCP Secret Manager, etc.
- A cluster bootstrap step that creates the Secret with `kubectl create secret`
  using values pulled from the organization's secret store.

In all cases the manifest checked into git must not contain real credentials.

## Rolling updates

Every microservice Deployment is configured for zero-downtime rollouts:

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1
    maxUnavailable: 0
```

Combined with:

- `readinessProbe` on `/actuator/health/readiness` so K8s only routes traffic
  to pods that have passed startup.
- `livenessProbe` on `/actuator/health/liveness` so stuck pods get restarted.
- `startupProbe` on `/actuator/health` so slow first starts don't trigger
  liveness restarts during boot.
- `terminationGracePeriodSeconds: 30` + a `preStop` sleep so pods drain
  in-flight traffic before the container is killed.

## Horizontal Pod Autoscaling

Each service ships an `autoscaling/v2` HPA that scales on CPU (70% target)
and memory (80% target). Overlays override `minReplicas` / `maxReplicas`
per environment.

## MySQL

Migrated from `apps/v1beta1` to `apps/v1`. The StatefulSet now:

- References credentials exclusively via `ftgo-db-secret`.
- Reads database name from the `ftgo-db-config` ConfigMap.
- Defines `readinessProbe` / `livenessProbe` using `mysqladmin ping`.
- Uses explicit `resources.requests` / `resources.limits`.

## CI validation

`k8s-validate.yml` runs on every PR that touches a Kubernetes path and
performs:

1. `yaml.safe_load_all` on every manifest to catch parse errors.
2. `kustomize build` on every overlay.
3. `kubeconform -strict -ignore-missing-schemas` against the rendered output
   to validate schemas against the Kubernetes API.

Rendered manifests are uploaded as artifacts so reviewers can inspect the
fully-resolved output.
