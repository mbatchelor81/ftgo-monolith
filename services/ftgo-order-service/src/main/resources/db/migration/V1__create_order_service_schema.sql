-- =============================================================================
-- V1: Order Service — Initial Schema
-- =============================================================================
-- Database: ftgo_order_service
-- Service:  ftgo-order-service
-- Jira:     EM-29
--
-- This migration creates the Order Service's own database schema as part
-- of the database-per-service strategy.  The orders and order_line_items
-- tables were originally in the shared 'ftgo' database (V1__create_ftgo_db.sql).
--
-- Key differences from the monolith schema:
--   1. Dedicated database (ftgo_order_service) — no shared schema.
--   2. consumer_id, restaurant_id, and assigned_courier_id are stored as
--      plain BIGINT references — no FK constraints to other service tables.
--      Consistency is maintained via domain events.
--   3. Per-service AUTO_INCREMENT id generation replaces shared hibernate_sequence.
--   4. Added audit columns (created_at, updated_at) for event-driven consistency.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- orders — Central order entity with lifecycle state machine
-- ---------------------------------------------------------------------------
-- Cross-service references (no FK constraints):
--   consumer_id        -> Consumer Service (ftgo_consumer_service.consumers.id)
--   restaurant_id      -> Restaurant Service (ftgo_restaurant_service.restaurants.id)
--   assigned_courier_id -> Courier Service (ftgo_courier_service.couriers.id)
CREATE TABLE orders (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    order_state              VARCHAR(255) NOT NULL DEFAULT 'APPROVED',
    consumer_id              BIGINT       NOT NULL,
    restaurant_id            BIGINT       NOT NULL,
    assigned_courier_id      BIGINT,
    payment_token            VARCHAR(255),
    order_minimum            DECIMAL(19, 2),
    delivery_address_street1 VARCHAR(255),
    delivery_address_street2 VARCHAR(255),
    delivery_address_city    VARCHAR(255),
    delivery_address_state   VARCHAR(255),
    delivery_address_zip     VARCHAR(255),
    accept_time              DATETIME,
    preparing_time           DATETIME,
    ready_for_pickup_time    DATETIME,
    picked_up_time           DATETIME,
    delivered_time           DATETIME,
    delivery_time            DATETIME,
    ready_by                 DATETIME,
    previous_ticket_state    INTEGER,
    version                  BIGINT       NOT NULL DEFAULT 0,
    created_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Index for consumer order lookups (findAllByConsumerId)
CREATE INDEX idx_orders_consumer_id ON orders (consumer_id);

-- Index for restaurant order lookups
CREATE INDEX idx_orders_restaurant_id ON orders (restaurant_id);

-- Index for courier assignment lookups
CREATE INDEX idx_orders_assigned_courier_id ON orders (assigned_courier_id);

-- Index for order state queries
CREATE INDEX idx_orders_order_state ON orders (order_state);

-- ---------------------------------------------------------------------------
-- order_line_items — Individual items within an order
-- ---------------------------------------------------------------------------
CREATE TABLE order_line_items (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    order_id     BIGINT       NOT NULL,
    menu_item_id VARCHAR(255) NOT NULL,
    name         VARCHAR(255),
    price        DECIMAL(19, 2),
    quantity     INTEGER      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_line_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Index for order lookups
CREATE INDEX idx_order_line_items_order_id ON order_line_items (order_id);
