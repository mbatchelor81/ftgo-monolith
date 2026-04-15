package com.ftgo.testlib.templates;

import static org.assertj.core.api.Assertions.assertThat;

import com.ftgo.testlib.base.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Template for REST API tests using REST-Assured with a running Spring Boot context.
 *
 * <h2>How to use this template</h2>
 *
 * <ol>
 *   <li>Copy into your service's {@code src/integration-test/java} directory
 *   <li>Rename the class (e.g., {@code OrderApiTest})
 *   <li>Uncomment the {@code @SpringBootTest} annotation with your application class
 *   <li>Configure REST-Assured with the random port
 *   <li>Write tests for each API endpoint
 * </ol>
 *
 * <h2>Conventions</h2>
 *
 * <ul>
 *   <li>Use {@code @SpringBootTest(webEnvironment = RANDOM_PORT)} for a full server
 *   <li>Extend {@link BaseIntegrationTest} for Testcontainers setup
 *   <li>Group tests by endpoint using {@code @Nested} classes
 *   <li>Test status codes, response bodies, and error handling
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * @SpringBootTest(
 *     classes = OrderServiceApplication.class,
 *     webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 * class OrderApiTest extends BaseIntegrationTest {
 *
 *     @LocalServerPort
 *     private int port;
 *
 *     @BeforeEach
 *     void setUp() {
 *         RestAssured.port = port;
 *     }
 *
 *     @Test
 *     void getOrders_shouldReturn200() {
 *         given()
 *             .contentType(ContentType.JSON)
 *         .when()
 *             .get("/api/orders")
 *         .then()
 *             .statusCode(200)
 *             .body("size()", greaterThan(0));
 *     }
 * }
 * }</pre>
 *
 * @see io.restassured.RestAssured
 * @see BaseIntegrationTest
 */
// @SpringBootTest(
//     classes = YourServiceApplication.class,
//     webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("ApiTestTemplate — copy and rename")
@SuppressWarnings(
        "checkstyle:MethodName") // Template uses test naming convention: method_condition_result
public class ApiTestTemplate extends BaseIntegrationTest {

    // @LocalServerPort
    // private int port;

    // @BeforeEach
    // void setUp() {
    //     io.restassured.RestAssured.port = port;
    // }

    @Nested
    @DisplayName("GET /api/resource")
    class GetAll {

        @Test
        @DisplayName("should return 200 with list of resources")
        void getAll_shouldReturn200() {
            // given()
            //     .contentType(io.restassured.http.ContentType.JSON)
            // .when()
            //     .get("/api/resource")
            // .then()
            //     .statusCode(200);
            assertThat(true).isTrue(); // Placeholder
        }
    }

    @Nested
    @DisplayName("POST /api/resource")
    class Create {

        @Test
        @DisplayName("should return 201 with valid input")
        void create_withValidInput_shouldReturn201() {
            // String body = "{\"name\":\"test\"}";
            //
            // given()
            //     .contentType(io.restassured.http.ContentType.JSON)
            //     .body(body)
            // .when()
            //     .post("/api/resource")
            // .then()
            //     .statusCode(201)
            //     .body("id", notNullValue());
            assertThat(true).isTrue(); // Placeholder
        }

        @Test
        @DisplayName("should return 400 with invalid input")
        void create_withInvalidInput_shouldReturn400() {
            // given()
            //     .contentType(io.restassured.http.ContentType.JSON)
            //     .body("{}")
            // .when()
            //     .post("/api/resource")
            // .then()
            //     .statusCode(400);
            assertThat(true).isTrue(); // Placeholder
        }
    }

    @Nested
    @DisplayName("GET /api/resource/{id}")
    class GetById {

        @Test
        @DisplayName("should return 404 when not found")
        void getById_whenNotFound_shouldReturn404() {
            // given()
            //     .contentType(io.restassured.http.ContentType.JSON)
            // .when()
            //     .get("/api/resource/999")
            // .then()
            //     .statusCode(404);
            assertThat(true).isTrue(); // Placeholder
        }
    }
}
