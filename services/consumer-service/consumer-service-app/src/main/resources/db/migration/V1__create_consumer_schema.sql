-- =============================================================================
-- Consumer Service — Initial Schema
-- Database: ftgo_consumer_service
--
-- Migrated from monolith table: consumers (ftgo database)
-- Changes from monolith:
--   - ID generation: hibernate_sequence (TABLE) -> AUTO_INCREMENT (IDENTITY)
--   - Standalone database — no cross-service foreign keys
-- =============================================================================

CREATE TABLE consumers (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;
