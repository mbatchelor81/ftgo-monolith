# Zipkin Tracing Infrastructure

## Quick Start

```bash
cd infrastructure/tracing
docker-compose -f docker-compose-zipkin.yml up -d
```

Zipkin UI will be available at **http://localhost:9411**.

## Architecture

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│ Order Service│     │Consumer Svc  │     │Restaurant Svc│
│  (Brave)     │     │  (Brave)     │     │  (Brave)     │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                    │
       │  POST /api/v2/spans (HTTP)              │
       └────────────┬───────┘────────────────────┘
                    ▼
            ┌───────────────┐
            │  Zipkin Server│
            │  :9411        │
            └───────────────┘
```

Each service uses `ftgo-tracing-lib` which configures:
1. **Brave** as the tracing implementation
2. **Micrometer Tracing** as the facade
3. **Zipkin Reporter** to export spans via HTTP

## Storage Options

| Storage | `STORAGE_TYPE` | Use Case |
|---------|---------------|----------|
| In-Memory | `mem` | Development / CI |
| MySQL | `mysql` | Small deployments |
| Elasticsearch | `elasticsearch` | Production |
| Cassandra | `cassandra` | High-scale production |

### Elasticsearch Example

```yaml
environment:
  STORAGE_TYPE: elasticsearch
  ES_HOSTS: http://elasticsearch:9200
```

## Service Configuration

Each service should set:

```yaml
ftgo:
  tracing:
    service-name: <service-name>
    zipkin:
      endpoint: http://zipkin:9411/api/v2/spans
```

## Health Check

```bash
curl http://localhost:9411/health
# {"status":"UP"}
```
