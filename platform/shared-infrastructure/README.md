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

**Scaffold only** — contents will grow as the platform work (EM-34, EM-35)
lands.
