package com.ftgo.order.web;

import com.ftgo.testlib.base.BaseContractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Contract test template for Order API endpoints.
 * Uses @WebMvcTest for lightweight controller testing with MockMvc,
 * verifying request/response contracts without a full Spring context.
 *
 * <p>For Spring Cloud Contract, extend this base class and define
 * contract DSL files under src/test/resources/contracts/.</p>
 *
 * <p>NOTE: This is a template. The @WebMvcTest annotation is commented out
 * because OrderController requires service-layer beans that are not yet
 * available as standalone microservice components. Uncomment and add
 * @MockBean declarations once service extraction is complete.</p>
 */
// @WebMvcTest(OrderController.class) — uncomment when service layer is extracted
@DisplayName("Order API Contract Tests")
class OrderControllerContractTest extends BaseContractTest {

    // @Autowired
    // private MockMvc mockMvc;

    // @MockBean
    // private OrderService orderService;

    // @BeforeEach
    // void setUp() {
    //     initMockMvc(mockMvc);
    // }

    // --- Template: uncomment and adapt once service extraction is complete ---

    // @Test
    // void getOrder_whenExists_returns200WithOrderJson() {
    //     // Arrange
    //     when(orderService.findById(1L)).thenReturn(Optional.of(testOrder));
    //
    //     // Act & Assert
    //     given()
    //         .contentType("application/json")
    //     .when()
    //         .get("/orders/{id}", 1L)
    //     .then()
    //         .statusCode(200)
    //         .body("id", equalTo(1))
    //         .body("orderState", equalTo("APPROVED"));
    // }

    // @Test
    // void getOrder_whenNotFound_returns404() {
    //     // Arrange
    //     when(orderService.findById(999L)).thenReturn(Optional.empty());
    //
    //     // Act & Assert
    //     given()
    //         .contentType("application/json")
    //     .when()
    //         .get("/orders/{id}", 999L)
    //     .then()
    //         .statusCode(404);
    // }
}
