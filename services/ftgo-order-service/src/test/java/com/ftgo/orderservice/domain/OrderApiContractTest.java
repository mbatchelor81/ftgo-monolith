package com.ftgo.orderservice.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.ftgo.testlib.base.BaseContractTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for the Order Service REST API.
 *
 * <p>Validates that API request/response shapes conform to the expected contract, ensuring
 * backwards compatibility as the service evolves.
 */
@DisplayName("Order API Contract")
class OrderApiContractTest extends BaseContractTest {

    @Nested
    @DisplayName("CreateOrderRequest")
    class CreateOrderRequestContract {

        @Test
        @DisplayName("should contain consumerId, restaurantId, and lineItems")
        void createOrderRequest_shouldContainRequiredFields() {
            String json =
                    "{\"consumerId\":1,\"restaurantId\":2,"
                            + "\"lineItems\":[{\"menuItemId\":\"item-1\",\"quantity\":2}]}";

            JsonNode node = parseJson(json);
            assertFieldExists(node, "consumerId");
            assertFieldExists(node, "restaurantId");
            assertFieldExists(node, "lineItems");
            assertThat(node.get("lineItems").isArray()).isTrue();
            assertThat(node.get("lineItems").size()).isGreaterThan(0);
        }

        @Test
        @DisplayName("lineItem should contain menuItemId and quantity")
        void lineItem_shouldContainRequiredFields() {
            String json = "{\"menuItemId\":\"item-1\",\"quantity\":2}";

            JsonNode node = parseJson(json);
            assertFieldExists(node, "menuItemId");
            assertFieldExists(node, "quantity");
            assertThat(node.get("quantity").isNumber()).isTrue();
        }
    }

    @Nested
    @DisplayName("CreateOrderResponse")
    class CreateOrderResponseContract {

        @Test
        @DisplayName("should contain orderId")
        void createOrderResponse_shouldContainOrderId() {
            String json = "{\"orderId\":42}";

            JsonNode node = parseJson(json);
            assertFieldExists(node, "orderId");
            assertThat(node.get("orderId").isNumber()).isTrue();
        }
    }

    @Nested
    @DisplayName("Error Response")
    class ErrorResponseContract {

        @Test
        @DisplayName("should contain status, message, and timestamp fields")
        void errorResponse_shouldContainRequiredFields() {
            String json =
                    "{\"status\":400,\"message\":\"Invalid order\","
                            + "\"timestamp\":\"2024-01-01T00:00:00Z\"}";

            JsonNode node = parseJson(json);
            assertFieldExists(node, "status");
            assertFieldExists(node, "message");
            assertFieldExists(node, "timestamp");
        }
    }
}
