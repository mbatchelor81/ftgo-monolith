-- =============================================================================
-- Consumer Service — Initial Schema
-- =============================================================================
-- Owns: consumers table
-- ID Strategy: IDENTITY (auto_increment) — replaces shared hibernate_sequence
-- No cross-service foreign keys
-- =============================================================================

CREATE TABLE IF NOT EXISTS consumers
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;
