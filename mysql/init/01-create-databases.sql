-- =============================================================================
-- Initialize per-service databases for local development
-- =============================================================================
-- This script runs automatically when the MySQL container starts for the
-- first time (via docker-entrypoint-initdb.d).
-- =============================================================================

CREATE DATABASE IF NOT EXISTS ftgo_order_service;
CREATE DATABASE IF NOT EXISTS ftgo_consumer_service;
CREATE DATABASE IF NOT EXISTS ftgo_restaurant_service;
CREATE DATABASE IF NOT EXISTS ftgo_courier_service;

-- Grant access to the application user
GRANT ALL PRIVILEGES ON ftgo_order_service.* TO 'mysqluser'@'%';
GRANT ALL PRIVILEGES ON ftgo_consumer_service.* TO 'mysqluser'@'%';
GRANT ALL PRIVILEGES ON ftgo_restaurant_service.* TO 'mysqluser'@'%';
GRANT ALL PRIVILEGES ON ftgo_courier_service.* TO 'mysqluser'@'%';
FLUSH PRIVILEGES;
