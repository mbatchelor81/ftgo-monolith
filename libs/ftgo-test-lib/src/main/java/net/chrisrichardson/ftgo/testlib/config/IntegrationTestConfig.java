package net.chrisrichardson.ftgo.testlib.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Composed annotation for FTGO integration tests.
 *
 * <p>Combines {@code @SpringBootTest}, {@code @Testcontainers}, and activates
 * the {@code test} profile. Use this on integration test classes to reduce
 * boilerplate.
 *
 * <p>Usage:
 * <pre>
 * {@literal @}FtgoIntegrationTest
 * class OrderRepositoryIntegrationTest {
 *
 *     {@literal @}Container
 *     static MySQLContainer{@literal <?>} mysql = FtgoMySQLContainer.getInstance();
 *
 *     {@literal @}DynamicPropertySource
 *     static void configureDataSource(DynamicPropertyRegistry registry) {
 *         registry.add("spring.datasource.url", mysql::getJdbcUrl);
 *         registry.add("spring.datasource.username", mysql::getUsername);
 *         registry.add("spring.datasource.password", mysql::getPassword);
 *     }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public @interface IntegrationTestConfig {
}
