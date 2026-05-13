-- =============================================================================
-- Courier Service — Initial Schema
-- Database: ftgo_courier_service
--
-- Migrated from monolith tables: courier, courier_actions (ftgo database)
-- Changes from monolith:
--   - Removed FK: courier_actions.order_id -> orders.id (cross-service)
--   - Retained FK: courier_actions.courier_id -> courier.id (intra-service)
--   - Added index on courier_actions.order_id for query performance
-- =============================================================================

CREATE TABLE courier (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    available  BIT,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    street1    VARCHAR(255),
    street2    VARCHAR(255),
    city       VARCHAR(255),
    state      VARCHAR(255),
    zip        VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE courier_actions (
    courier_id BIGINT       NOT NULL,
    order_id   BIGINT,
    time       DATETIME,
    type       VARCHAR(255),
    INDEX idx_courier_actions_courier_id (courier_id),
    INDEX idx_courier_actions_order_id (order_id),
    CONSTRAINT fk_courier_actions_courier
        FOREIGN KEY (courier_id) REFERENCES courier (id)
) ENGINE = InnoDB;
