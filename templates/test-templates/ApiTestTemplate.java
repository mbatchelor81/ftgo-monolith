package com.ftgo.example.api;

import com.ftgo.test.containers.AbstractIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * FTGO API-test template.
 *
 * <p>ArchitectureNotes
 * ------------------
 * An API test in FTGO:
 *   - Boots the full Spring Boot application on a random HTTP port
 *     ({@code webEnvironment = RANDOM_PORT}) — no MockMvc shortcut.
 *   - Drives traffic with Rest-Assured, so the test exercises every
 *     part of the HTTP stack: Jackson, CORS, security filters, error
 *     mappers.
 *   - Uses {@link AbstractIntegrationTest} so SQL comes from a real
 *     MySQL container rather than an in-memory substitute.
 *
 * <p>API tests cover <strong>golden paths</strong>. Branch coverage
 * lives in unit and integration tests (see
 * {@code docs/testing/when-to-write-which-test.md}). Target ~5-10 API
 * tests per bounded context.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiTestTemplate extends AbstractIntegrationTest {

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
    }

    @LocalServerPort
    private int port;

    @BeforeEach
    void configureRestAssured() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void createOrder_withValidPayload_returns201AndOrderBody() {
        String payload = """
                {
                  "consumerId": 42,
                  "restaurantId": 7,
                  "lineItems": [
                    { "menuItemId": "vindaloo", "quantity": 2 }
                  ]
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(payload)
            .when()
                .post("/orders")
            .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("state", equalTo("APPROVED"))
                .body("consumerId", equalTo(42));
    }

    @Test
    void getOrder_whenMissing_returns404WithJsonProblem() {
        given()
                .accept(ContentType.JSON)
            .when()
                .get("/orders/{id}", 999999)
            .then()
                .statusCode(404)
                .body("error", equalTo("Not Found"));
    }
}
