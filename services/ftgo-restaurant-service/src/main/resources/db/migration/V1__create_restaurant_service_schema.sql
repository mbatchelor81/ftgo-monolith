-- =============================================================================
-- V1: Restaurant Service — Initial Schema
-- =============================================================================
-- Database: ftgo_restaurant_service
-- Service:  ftgo-restaurant-service
-- Jira:     EM-29
--
-- This migration creates the Restaurant Service's own database schema as part
-- of the database-per-service strategy.  The restaurants and
-- restaurant_menu_items tables were originally in the shared 'ftgo' database
-- (V1__create_ftgo_db.sql).
--
-- Key differences from the monolith schema:
--   1. Dedicated database (ftgo_restaurant_service) — no shared schema.
--   2. No cross-service foreign keys.
--   3. Per-service AUTO_INCREMENT id generation replaces shared hibernate_sequence.
--   4. Added audit columns (created_at, updated_at) for event-driven consistency.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- restaurants — Restaurant profile and address
-- ---------------------------------------------------------------------------
CREATE TABLE restaurants (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    street1    VARCHAR(255),
    street2    VARCHAR(255),
    city       VARCHAR(255),
    state      VARCHAR(255),
    zip        VARCHAR(255),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Index for name-based searches
CREATE INDEX idx_restaurants_name ON restaurants (name);

-- ---------------------------------------------------------------------------
-- restaurant_menu_items — Menu items belonging to a restaurant
-- ---------------------------------------------------------------------------
CREATE TABLE restaurant_menu_items (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    restaurant_id BIGINT       NOT NULL,
    menu_item_id  VARCHAR(255) NOT NULL,
    name          VARCHAR(255),
    price         DECIMAL(19, 2),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_menu_items_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Index for restaurant lookups
CREATE INDEX idx_menu_items_restaurant_id ON restaurant_menu_items (restaurant_id);
