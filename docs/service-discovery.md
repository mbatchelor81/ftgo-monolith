# Service Discovery

## Overview

FTGO uses [HashiCorp Consul](https://www.consul.io/) for service discovery, enabling
microservices to locate each other dynamically without hard-coded hostnames or manual
DNS configuration.

## Architecture

```
┌─────────────────┐     register/heartbeat     ┌──────────────┐
│  Order Service  │ ──────────────────────────▸ │              │
├─────────────────┤                             │    Consul    │
│ Consumer Service│ ──────────────────────────▸ │    Server    │
├─────────────────┤                             │              │
│Restaurant Service│──────────────────────────▸ │  (port 8500) │
├─────────────────┤                             │              │
│ Courier Service │ ──────────────────────────▸ │              │
└─────────────────┘                             └──────┬───────┘
                                                       │
                        ┌──────────────────────────────┘
                        │  lookup (DNS / HTTP API)
                        ▾
                 Service Consumer
```

Each service registers itself with Consul on startup, providing:
- Service name and instance ID
- Host/IP and port
- Health check endpoint (`/actuator/health`)
- Tags and metadata for routing decisions

## Deployment Options

### Docker Compose (Local Development)

```bash
cd infrastructure/consul
docker-compose -f docker-compose.consul.yml up -d
```

Consul UI is available at `http://localhost:8500`.

### Kubernetes

Consul is included in the Kustomize base manifests:

```
deployment/kubernetes/base/consul/
├── kustomization.yaml
├── deployment.yaml
└── service.yaml
```

Access within the cluster: `http://ftgo-consul:8500`.

## Spring Boot Integration

Activate the `consul` profile to enable Spring Cloud Consul:

```bash
SPRING_PROFILES_ACTIVE=consul ./gradlew :order-service-app:bootRun
```

Reference configuration is at `infrastructure/consul/application-consul.yml`.

### Key Properties

| Property | Default | Description |
|----------|---------|-------------|
| `spring.cloud.consul.host` | `localhost` | Consul agent host |
| `spring.cloud.consul.port` | `8500` | Consul HTTP API port |
| `spring.cloud.consul.discovery.register` | `true` | Auto-register on startup |
| `spring.cloud.consul.discovery.prefer-ip-address` | `true` | Register IP instead of hostname |
| `spring.cloud.consul.discovery.health-check-path` | `/actuator/health` | Health check endpoint |
| `spring.cloud.consul.discovery.health-check-interval` | `15s` | Health check frequency |

### Environment Variables

Override defaults with environment variables (useful in Docker/K8s):

```yaml
env:
  - name: CONSUL_HOST
    value: ftgo-consul
  - name: CONSUL_PORT
    value: "8500"
  - name: SPRING_PROFILES_ACTIVE
    value: consul
```

## Health Checks

Consul performs periodic HTTP health checks against each registered service. A service
is considered healthy when `/actuator/health` returns HTTP 200. After failing health
checks for `deregister_critical_service_after` (default 90s), the service instance is
automatically deregistered.

### Health Check Flow

```
Consul Agent
    │
    ├──▸ GET /actuator/health  (every 15s)
    │        │
    │        ├── 200 OK    → service marked "passing"
    │        ├── 503       → service marked "warning"
    │        └── timeout   → service marked "critical"
    │
    └── After 90s critical → auto-deregister
```

## Service-to-Service Communication

With Consul discovery enabled, services resolve each other by name:

```java
// Spring Cloud DiscoveryClient (when available)
List<ServiceInstance> instances = discoveryClient.getInstances("order-service");

// Or via Spring Cloud LoadBalancer with @LoadBalanced RestTemplate
restTemplate.getForObject("http://order-service/api/orders/{id}", OrderDTO.class, id);
```

## Migration from Direct Invocation

The monolith currently uses direct method invocation between services. During the
microservices migration:

1. **Phase 1 (current):** Consul is deployed alongside the monolith. Services register
   but don't yet use discovery for communication.
2. **Phase 2:** Extracted microservices use Consul to discover each other via HTTP.
3. **Phase 3:** Full service mesh with Consul Connect for mTLS and traffic management.
