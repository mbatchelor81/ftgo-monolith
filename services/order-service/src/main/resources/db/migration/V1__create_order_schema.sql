-- =====================================================================
-- V1__create_order_schema.sql
-- ---------------------------------------------------------------------
-- Initial schema for the Order Service's private database
-- (`ftgo_order`).
--
-- Ownership: the Order bounded context is the sole owner of every
-- table in this schema. Other services may only observe these tables
-- via published domain events (see docs/database-migration-strategy.md).
--
-- Cross-service references:
--   * `orders.consumer_id`          references a consumer owned by the
--                                    Consumer Service; stored as a plain
--                                    BIGINT — NO foreign key.
--   * `orders.restaurant_id`        references a restaurant owned by the
--                                    Restaurant Service; stored as a
--                                    plain BIGINT — NO foreign key.
--   * `orders.assigned_courier_id`  references a courier owned by the
--                                    Courier Service; stored as a plain
--                                    BIGINT — NO foreign key.
--
-- Intra-service referential integrity is preserved:
--   * `order_line_items.order_id`   has an FK to `orders.id`.
-- =====================================================================

CREATE TABLE orders
(
    id                       BIGINT NOT NULL AUTO_INCREMENT,
    version                  BIGINT,
    order_state              VARCHAR(255),
    consumer_id              BIGINT,
    restaurant_id            BIGINT,
    assigned_courier_id      BIGINT,
    accept_time              DATETIME,
    preparing_time           DATETIME,
    ready_for_pickup_time    DATETIME,
    picked_up_time           DATETIME,
    delivered_time           DATETIME,
    delivery_time            DATETIME,
    ready_by                 DATETIME,
    previous_ticket_state    INTEGER,
    delivery_address_street1 VARCHAR(255),
    delivery_address_street2 VARCHAR(255),
    delivery_address_city    VARCHAR(255),
    delivery_address_state   VARCHAR(255),
    delivery_address_zip     VARCHAR(255),
    order_minimum            DECIMAL(19, 2),
    payment_token            VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE order_line_items
(
    order_id     BIGINT  NOT NULL,
    menu_item_id VARCHAR(255),
    name         VARCHAR(255),
    price        DECIMAL(19, 2),
    quantity     INTEGER NOT NULL
) ENGINE = InnoDB;

ALTER TABLE order_line_items
    ADD CONSTRAINT order_line_items_order_id
        FOREIGN KEY (order_id) REFERENCES orders (id);

-- Secondary indexes to support lookups that were previously served by
-- the monolith's FK indexes. These replace the implicit index support
-- that the dropped cross-service FKs used to provide.
CREATE INDEX idx_orders_consumer_id          ON orders (consumer_id);
CREATE INDEX idx_orders_restaurant_id        ON orders (restaurant_id);
CREATE INDEX idx_orders_assigned_courier_id  ON orders (assigned_courier_id);
