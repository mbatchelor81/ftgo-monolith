# FTGO EFK (Elasticsearch + Fluentd + Kibana) Stack

Centralized log aggregation for every FTGO microservice, introduced in
**EM-43**.

## Overview

Every microservice emits structured JSON log lines on stdout (see
`libs/ftgo-logging`). Fluentd runs as a DaemonSet on every node, tails
`/var/log/containers/*.log`, parses the JSON payload, enriches it with
Kubernetes metadata (namespace, pod, labels), and ships the result to
Elasticsearch. Kibana provides an operator-facing UI backed by
Elasticsearch.

```
pod(stdout JSON)
   └─▶ kubelet
        └─▶ /var/log/containers/*.log
             └─▶ Fluentd DaemonSet
                  └─▶ Elasticsearch StatefulSet
                       └─▶ Kibana Deployment
```

## Deploy

```bash
kubectl apply -k deployment/kubernetes/logging
```

This creates a dedicated `logging` namespace containing:

| Manifest | Component |
|---|---|
| `namespace.yaml` | `logging` namespace |
| `elasticsearch.yaml` | Single-node Elasticsearch StatefulSet + headless Service |
| `kibana.yaml` | Kibana Deployment + ClusterIP Service |
| `fluentd-rbac.yaml` | ServiceAccount + ClusterRole/Binding for pod metadata |
| `fluentd-configmap.yaml` | Fluentd tail → JSON parse → ES config |
| `fluentd-daemonset.yaml` | Fluentd DaemonSet tailing host container logs |
| `ilm-policy.yaml` | Job that installs the ILM retention policy |

## Retention

Logs are rolled over into the `ftgo-logs-*` index alias and pruned by
the `ftgo-logs-retention` ILM policy defined in `ilm-policy.yaml`:

- **Hot** (active writes): 0–7 days
- **Warm** (force-merged, read-only): 7–30 days
- **Delete**: after 30 days

Tune the numbers in `ilm-policy.yaml` per environment.

## Local development

For docker-compose-based local runs, use:

```bash
docker compose -f docker-compose.yml -f docker-compose.logging.yml up
```

Kibana: <http://localhost:5601>
