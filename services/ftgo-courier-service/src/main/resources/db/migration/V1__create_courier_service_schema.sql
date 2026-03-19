-- =============================================================================
-- Courier Service — Initial Schema
-- =============================================================================
-- Owns: courier, courier_actions tables
-- ID Strategy: IDENTITY (auto_increment) — replaces shared hibernate_sequence
-- Cross-service FK removed: courier_actions.order_id no longer references orders
-- =============================================================================

CREATE TABLE IF NOT EXISTS courier
(
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

CREATE TABLE IF NOT EXISTS courier_actions
(
    courier_id BIGINT       NOT NULL,
    order_id   BIGINT,
    time       DATETIME,
    type       VARCHAR(255),
    CONSTRAINT fk_courier_actions_courier FOREIGN KEY (courier_id) REFERENCES courier (id)
) ENGINE = InnoDB;

-- Note: order_id is stored as a plain BIGINT reference (no FK constraint).
-- The Order Service owns the orders table in its own database.
-- Data consistency is maintained via eventual consistency / events.
