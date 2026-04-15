#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Initializes Elasticsearch with FTGO ILM policies and index templates.
#
# Usage:
#   ./setup-elasticsearch.sh [ELASTICSEARCH_URL]
#
# Defaults to http://localhost:9200 if no argument is provided.
# ---------------------------------------------------------------------------
set -euo pipefail

ES_URL="${1:-http://localhost:9200}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Waiting for Elasticsearch at ${ES_URL} ..."
until curl -sf "${ES_URL}/_cluster/health" > /dev/null 2>&1; do
  sleep 2
done
echo "Elasticsearch is ready."

# 1. Create ILM policy
echo "Creating ILM policy: ftgo-log-retention ..."
curl -sf -X PUT "${ES_URL}/_ilm/policy/ftgo-log-retention" \
  -H 'Content-Type: application/json' \
  -d @"${SCRIPT_DIR}/ilm-policy.json"
echo ""

# 2. Create index template
echo "Creating index template: ftgo-logs ..."
curl -sf -X PUT "${ES_URL}/_index_template/ftgo-logs" \
  -H 'Content-Type: application/json' \
  -d @"${SCRIPT_DIR}/index-template.json"
echo ""

# 3. Register error-rate watcher alert
echo "Registering error-rate alert watcher ..."
curl -sf -X PUT "${ES_URL}/_watcher/watch/ftgo-error-rate-spike" \
  -H 'Content-Type: application/json' \
  -d @"${SCRIPT_DIR}/error-rate-alert.json" 2>/dev/null || echo "(watcher API may not be available in OSS — skipping)"
echo ""

echo "Elasticsearch setup complete."
