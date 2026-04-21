# Kustomize bundle

This directory composes the per-service Kustomize bases into a single
renderable bundle and layers environment-specific overrides on top.

```
kustomize/
├── base/
│   └── kustomization.yaml       # references stateful-services + each microservice
└── overlays/
    ├── dev/                     # namespace: ftgo-dev,     tag: dev,     1 replica
    ├── staging/                 # namespace: ftgo-staging, tag: staging, 2 replicas
    └── prod/                    # namespace: ftgo-prod,    tag: stable,  3+ replicas
```

## Render locally

```bash
kubectl kustomize deployment/kustomize/overlays/dev
kubectl kustomize deployment/kustomize/overlays/staging
kubectl kustomize deployment/kustomize/overlays/prod
```

## Apply to a cluster

```bash
# Apply shared platform resources once per cluster.
kubectl apply -k platform/shared-infrastructure/k8s/namespaces
kubectl apply -k platform/shared-infrastructure/k8s/rbac       -n ftgo-dev
kubectl apply -k platform/shared-infrastructure/k8s/rbac       -n ftgo-staging
kubectl apply -k platform/shared-infrastructure/k8s/rbac       -n ftgo-prod

# Then deploy an environment.
kubectl apply -k deployment/kustomize/overlays/dev
```

## Overlay differences

| Knob              | dev  | staging | prod  |
| ----------------- | ---- | ------- | ----- |
| Namespace         | ftgo-dev | ftgo-staging | ftgo-prod |
| Image tag         | dev  | staging | stable |
| Deployment replicas | 1  | 2       | 3     |
| HPA min replicas  | 1    | 2       | 3     |
| HPA max replicas  | 3    | 6       | 20    |
| CPU request       | 50m  | 100m    | 250m  |
| Memory request    | 128Mi| 256Mi   | 512Mi |

## Secrets

All overlays assume the `ftgo-db-secret` is populated externally (via
sealed-secrets, external-secrets, or a cloud KMS). The placeholder Secret
checked into `deployment/kubernetes/stateful-services/ftgo-db-secret.yml` is
for local/dev only and must be replaced in staging and prod clusters.
