#!/bin/bash
# Create the FTGO database secret in Kubernetes.
# Replace the placeholder values with actual credentials before running.
# Usage: MYSQL_ROOT_PASSWORD=<pwd> MYSQL_USER=<user> MYSQL_PASSWORD=<pwd> ./create-db-secret.sh

kubectl create secret generic ftgo-db-secret \
  --from-literal=root-password="${MYSQL_ROOT_PASSWORD:?Must set MYSQL_ROOT_PASSWORD}" \
  --from-literal=username="${MYSQL_USER:-mysqluser}" \
  --from-literal=password="${MYSQL_PASSWORD:?Must set MYSQL_PASSWORD}"
