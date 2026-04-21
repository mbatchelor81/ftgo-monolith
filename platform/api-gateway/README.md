# API Gateway

Edge proxy for the FTGO platform. Terminates TLS, authenticates requests,
enforces rate limits, and routes to the correct downstream microservice.

**Scaffold only.** Implementation is tracked by EM-38 and EM-39.

Expected contents when built out:

- `Dockerfile` / `build.gradle` for the gateway binary (Spring Cloud Gateway
  or equivalent).
- `config/routes.yml` — declarative route table.
- `k8s/` — Deployment, Service, Ingress manifests.
