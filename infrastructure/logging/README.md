# FTGO Logging Infrastructure — ELK Stack

## Quick Start

```bash
cd infrastructure/logging
docker-compose -f docker-compose.logging.yml up -d
```

## Access

| Component | URL | Purpose |
|-----------|-----|---------|
| Elasticsearch | http://localhost:9200 | Log storage and search |
| Kibana | http://localhost:5601 | Log visualization and dashboards |
| Logstash | http://localhost:9600 | Pipeline monitoring |
| Logstash TCP | localhost:5000 | JSON log ingestion (TCP) |
| Logstash Beats | localhost:5044 | Filebeat log shipping |

## Architecture

```
Services (JSON logs) ──TCP:5000──▶ Logstash ──▶ Elasticsearch ──▶ Kibana
                      Beats:5044──┘
```

## Importing Kibana Dashboards

After Kibana is running, import the pre-built dashboards:

```bash
curl -X POST "http://localhost:5601/api/saved_objects/_import" \
  -H "kbn-xsrf: true" \
  --form file=@kibana/dashboards/ftgo-log-overview.ndjson
```

## Index Pattern

Logs are indexed as `ftgo-logs-{serviceName}-{date}`.
Create the index pattern `ftgo-logs-*` in Kibana to view all service logs.

## Stopping

```bash
docker-compose -f docker-compose.logging.yml down
# To also remove stored data:
docker-compose -f docker-compose.logging.yml down -v
```
