package com.ftgo.testlib.base;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for API tests using Rest-Assured.
 *
 * <p>Provides:</p>
 * <ul>
 *   <li>Full Spring context on a random port</li>
 *   <li>Rest-Assured auto-configured with the server port</li>
 *   <li>"api" tag and "test" profile</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * class OrderApiTest extends BaseApiTest {
 *
 *     @Test
 *     void getOrders_returnsOk() {
 *         given()
 *             .contentType(ContentType.JSON)
 *         .when()
 *             .get("/orders")
 *         .then()
 *             .statusCode(200);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Guidelines:</strong></p>
 * <ul>
 *   <li>Test full HTTP request/response cycle including serialization</li>
 *   <li>Validate status codes, response bodies, and headers</li>
 *   <li>Use for contract verification and API regression testing</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("api")
public abstract class BaseApiTest {

    @LocalServerPort
    protected int port;

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
        RestAssured.basePath = "/";
    }
}
