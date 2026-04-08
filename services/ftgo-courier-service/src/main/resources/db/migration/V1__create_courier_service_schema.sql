-- =============================================================================
-- V1: Courier Service — Initial Schema
-- =============================================================================
-- Database: ftgo_courier_service
-- Service:  ftgo-courier-service
-- Jira:     EM-29
--
-- This migration creates the Courier Service's own database schema as part
-- of the database-per-service strategy.  The courier and courier_actions
-- tables were originally in the shared 'ftgo' database (V1__create_ftgo_db.sql).
--
-- Key differences from the monolith schema:
--   1. Dedicated database (ftgo_courier_service) — no shared schema.
--   2. courier_actions.order_id is stored as a plain BIGINT reference (no FK
--      to orders) — the Order Service owns that entity in its own database.
--   3. Per-service AUTO_INCREMENT id generation replaces shared hibernate_sequence.
--   4. Added audit columns (created_at, updated_at) for event-driven consistency.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- couriers — Courier profile and availability tracking
-- ---------------------------------------------------------------------------
-- Note: table renamed from 'courier' (singular) to 'couriers' (plural) to
-- follow the project convention (see docs/CONVENTIONS.md § Database Naming).
CREATE TABLE couriers (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    available  BIT          NOT NULL DEFAULT 0,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    street1    VARCHAR(255),
    street2    VARCHAR(255),
    city       VARCHAR(255),
    state      VARCHAR(255),
    zip        VARCHAR(255),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Index for availability queries (findAllAvailable)
CREATE INDEX idx_couriers_available ON couriers (available);

-- ---------------------------------------------------------------------------
-- courier_actions — Pickup and drop-off plan entries for a courier
-- ---------------------------------------------------------------------------
-- order_id is a cross-service reference to the Order Service's orders table.
-- It is stored as a plain BIGINT — no foreign key constraint.  Consistency
-- is maintained via domain events (OrderAssigned, OrderPickedUp, etc.).
CREATE TABLE courier_actions (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    courier_id BIGINT       NOT NULL,
    order_id   BIGINT,
    time       DATETIME,
    type       VARCHAR(255),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_courier_actions_courier
        FOREIGN KEY (courier_id) REFERENCES couriers (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Index for looking up actions by courier
CREATE INDEX idx_courier_actions_courier_id ON courier_actions (courier_id);

-- Index for looking up actions by order (cross-service reference)
CREATE INDEX idx_courier_actions_order_id ON courier_actions (order_id);
