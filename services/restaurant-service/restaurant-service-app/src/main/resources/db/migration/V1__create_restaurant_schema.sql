-- =============================================================================
-- Restaurant Service — Initial Schema
-- Database: ftgo_restaurant_service
--
-- Migrated from monolith tables: restaurants, restaurant_menu_items (ftgo database)
-- Changes from monolith:
--   - No structural changes — all tables and relationships are self-contained
--   - Standalone database — no cross-service foreign keys existed
-- =============================================================================

CREATE TABLE restaurants (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255),
    street1 VARCHAR(255),
    street2 VARCHAR(255),
    city    VARCHAR(255),
    state   VARCHAR(255),
    zip     VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE restaurant_menu_items (
    restaurant_id BIGINT       NOT NULL,
    id            VARCHAR(255),
    name          VARCHAR(255),
    price         DECIMAL(19, 2),
    INDEX idx_restaurant_menu_items_restaurant_id (restaurant_id),
    CONSTRAINT fk_restaurant_menu_items_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
) ENGINE = InnoDB;
