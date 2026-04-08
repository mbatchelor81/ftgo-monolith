#!/usr/bin/env bash
# =============================================================================
# FTGO Elasticsearch ILM Policy Setup
# =============================================================================
# Creates Index Lifecycle Management policies for log retention.
#
# Usage:
#   ./setup-ilm.sh [ELASTICSEARCH_URL]
#
# Defaults to http://localhost:9200 if no URL is provided.
# =============================================================================

set -euo pipefail

ES_URL="${1:-http://localhost:9200}"

echo "Setting up FTGO ILM policies on ${ES_URL}..."

# --- Dev policy: 7 days retention ---
echo "Creating ILM policy: ftgo-logs-dev (7d retention)..."
curl -sf -X PUT "${ES_URL}/_ilm/policy/ftgo-logs-dev" \
  -H 'Content-Type: application/json' \
  -d '{
    "policy": {
      "phases": {
        "hot": {
          "min_age": "0ms",
          "actions": {
            "rollover": {
              "max_primary_shard_size": "10gb",
              "max_age": "1d"
            },
            "set_priority": { "priority": 100 }
          }
        },
        "delete": {
          "min_age": "7d",
          "actions": { "delete": {} }
        }
      }
    }
  }'
echo ""

# --- Staging policy: 30 days retention ---
echo "Creating ILM policy: ftgo-logs-staging (30d retention)..."
curl -sf -X PUT "${ES_URL}/_ilm/policy/ftgo-logs-staging" \
  -H 'Content-Type: application/json' \
  -d '{
    "policy": {
      "phases": {
        "hot": {
          "min_age": "0ms",
          "actions": {
            "rollover": {
              "max_primary_shard_size": "25gb",
              "max_age": "1d"
            },
            "set_priority": { "priority": 100 }
          }
        },
        "warm": {
          "min_age": "7d",
          "actions": {
            "shrink": { "number_of_shards": 1 },
            "forcemerge": { "max_num_segments": 1 },
            "set_priority": { "priority": 50 }
          }
        },
        "delete": {
          "min_age": "30d",
          "actions": { "delete": {} }
        }
      }
    }
  }'
echo ""

# --- Prod policy: 90 days retention ---
echo "Creating ILM policy: ftgo-logs-prod (90d retention)..."
curl -sf -X PUT "${ES_URL}/_ilm/policy/ftgo-logs-prod" \
  -H 'Content-Type: application/json' \
  -d '{
    "policy": {
      "phases": {
        "hot": {
          "min_age": "0ms",
          "actions": {
            "rollover": {
              "max_primary_shard_size": "50gb",
              "max_age": "1d"
            },
            "set_priority": { "priority": 100 }
          }
        },
        "warm": {
          "min_age": "7d",
          "actions": {
            "shrink": { "number_of_shards": 1 },
            "forcemerge": { "max_num_segments": 1 },
            "set_priority": { "priority": 50 }
          }
        },
        "cold": {
          "min_age": "30d",
          "actions": {
            "set_priority": { "priority": 0 }
          }
        },
        "delete": {
          "min_age": "90d",
          "actions": { "delete": {} }
        }
      }
    }
  }'
echo ""

# --- Create index template with ILM policy ---
echo "Creating index template: ftgo-logs..."
curl -sf -X PUT "${ES_URL}/_index_template/ftgo-logs" \
  -H 'Content-Type: application/json' \
  -d '{
    "index_patterns": ["ftgo-logs-*"],
    "template": {
      "settings": {
        "number_of_shards": 1,
        "number_of_replicas": 1,
        "index.lifecycle.name": "ftgo-logs-dev",
        "index.lifecycle.rollover_alias": "ftgo-logs"
      }
    },
    "priority": 100
  }'
echo ""

echo "ILM setup complete. Use Kibana or the API to switch environments:"
echo "  Dev:     PUT /_index_template/ftgo-logs  → lifecycle.name = ftgo-logs-dev"
echo "  Staging: PUT /_index_template/ftgo-logs  → lifecycle.name = ftgo-logs-staging"
echo "  Prod:    PUT /_index_template/ftgo-logs  → lifecycle.name = ftgo-logs-prod"
