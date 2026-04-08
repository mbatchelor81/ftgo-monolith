-- =============================================================================
-- V1: Consumer Service — Initial Schema
-- =============================================================================
-- Database: ftgo_consumer_service
-- Service:  ftgo-consumer-service
-- Jira:     EM-29
--
-- This migration creates the Consumer Service's own database schema as part
-- of the database-per-service strategy.  The consumer table was originally
-- in the shared 'ftgo' database (V1__create_ftgo_db.sql).
--
-- Key differences from the monolith schema:
--   1. Dedicated database (ftgo_consumer_service) — no shared schema.
--   2. No cross-service foreign keys.
--   3. Per-service AUTO_INCREMENT id generation replaces shared hibernate_sequence.
--   4. Added audit columns (created_at, updated_at) for event-driven consistency.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- consumers — Core entity for consumer registration and validation
-- ---------------------------------------------------------------------------
CREATE TABLE consumers (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Index for name lookups
CREATE INDEX idx_consumers_last_name ON consumers (last_name);
