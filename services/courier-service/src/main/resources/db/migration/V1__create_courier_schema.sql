-- =====================================================================
-- V1__create_courier_schema.sql
-- ---------------------------------------------------------------------
-- Initial schema for the Courier Service's private database
-- (`ftgo_courier`).
--
-- Ownership: the Courier bounded context is the sole owner of every
-- table in this schema.
--
-- Cross-service references:
--   * `courier_actions.order_id`    references an order owned by the
--                                    Order Service; stored as a plain
--                                    BIGINT — NO foreign key.
--
-- Intra-service referential integrity is preserved:
--   * `courier_actions.courier_id`  has an FK to `courier.id`.
-- =====================================================================

CREATE TABLE courier
(
    id         BIGINT NOT NULL AUTO_INCREMENT,
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

CREATE TABLE courier_actions
(
    courier_id BIGINT NOT NULL,
    order_id   BIGINT,
    time       DATETIME,
    type       VARCHAR(255)
) ENGINE = InnoDB;

ALTER TABLE courier_actions
    ADD CONSTRAINT courier_actions_courier_id
        FOREIGN KEY (courier_id) REFERENCES courier (id);

-- Supports lookups by external order id (e.g. "find all actions for
-- order X") that previously relied on the cross-service FK index.
CREATE INDEX idx_courier_actions_order_id ON courier_actions (order_id);
