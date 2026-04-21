-- =====================================================================
-- V1__create_consumer_schema.sql
-- ---------------------------------------------------------------------
-- Initial schema for the Consumer Service's private database
-- (`ftgo_consumer`).
--
-- Ownership: the Consumer bounded context is the sole owner of every
-- table in this schema. No other service may read from or write to
-- these tables directly — cross-service access goes through
-- consumer-service REST APIs (see docs/database-migration-strategy.md).
--
-- ID generation: each service now owns its own identity strategy. The
-- shared monolith `hibernate_sequence` table is replaced in this
-- schema by native MySQL `AUTO_INCREMENT` on the primary key. The
-- Consumer JPA entity is expected to be updated to
-- `@GeneratedValue(strategy = GenerationType.IDENTITY)` as part of the
-- code extraction that accompanies this migration.
-- =====================================================================

CREATE TABLE consumers
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;
