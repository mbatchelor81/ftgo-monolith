package com.ftgo.resilience.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Custom health indicator that verifies database connectivity.
 *
 * <p>Performs a lightweight validation query ({@code SELECT 1}) to confirm
 * the database is reachable and responding. Reports the database product
 * name and version in the health details.
 *
 * <p>This indicator is registered under the key {@code ftgoDatabase} in the
 * actuator health endpoint.
 */
@Component("ftgoDatabase")
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthIndicator.class);

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {

            if (resultSet.next()) {
                String dbProduct = connection.getMetaData().getDatabaseProductName();
                String dbVersion = connection.getMetaData().getDatabaseProductVersion();
                return Health.up()
                        .withDetail("database", dbProduct)
                        .withDetail("version", dbVersion)
                        .withDetail("validationQuery", "SELECT 1")
                        .build();
            }
            return Health.down()
                    .withDetail("error", "Validation query returned no results")
                    .build();

        } catch (Exception e) {
            log.error("Database health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
