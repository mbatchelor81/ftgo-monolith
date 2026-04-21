# Shared Infrastructure

Reusable Kubernetes primitives and platform-wide manifests that apply
regardless of which service is being deployed: namespaces, network policies,
RBAC roles, cluster-wide ConfigMaps, and the like.

Layout:

```
shared-infrastructure/
└── k8s/
    ├── namespaces/
    ├── network-policies/
    └── rbac/
```

Populated by EM-35:

- `namespaces/namespaces.yaml` — `ftgo-dev`, `ftgo-staging`, `ftgo-prod` with
  Pod Security Standards labels.
- `network-policies/default-deny.yaml` — default-deny baseline plus allow
  rules for DNS and intra-namespace traffic. Apply per-namespace.
- `rbac/deployer.yaml` — `ftgo-deployer` ServiceAccount + namespaced Role +
  RoleBinding used by the CD pipeline.

Apply once per cluster:

```bash
kubectl apply -k platform/shared-infrastructure/k8s/namespaces
for ns in ftgo-dev ftgo-staging ftgo-prod; do
  kubectl apply -k platform/shared-infrastructure/k8s/rbac -n "$ns"
  kubectl apply -k platform/shared-infrastructure/k8s/network-policies -n "$ns"
done
```

See `docs/kubernetes-deployment.md` for the end-to-end deployment workflow.
