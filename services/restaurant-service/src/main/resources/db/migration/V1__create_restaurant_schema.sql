-- =====================================================================
-- V1__create_restaurant_schema.sql
-- ---------------------------------------------------------------------
-- Initial schema for the Restaurant Service's private database
-- (`ftgo_restaurant`).
--
-- Ownership: the Restaurant bounded context is the sole owner of every
-- table in this schema. Menu data is published to other services via
-- domain events (see docs/database-migration-strategy.md §6).
--
-- Intra-service referential integrity is preserved:
--   * `restaurant_menu_items.restaurant_id` has an FK to `restaurants.id`.
-- =====================================================================

CREATE TABLE restaurants
(
    id      BIGINT NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255),
    street1 VARCHAR(255),
    street2 VARCHAR(255),
    city    VARCHAR(255),
    state   VARCHAR(255),
    zip     VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE restaurant_menu_items
(
    restaurant_id BIGINT NOT NULL,
    id            VARCHAR(255),
    name          VARCHAR(255),
    price         DECIMAL(19, 2)
) ENGINE = InnoDB;

ALTER TABLE restaurant_menu_items
    ADD CONSTRAINT restaurant_menu_items_restaurant_id
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id);
