package com.ftgo.testlib.templates;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.ftgo.testlib.base.BaseContractTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Template for API contract tests that validate REST response shapes.
 *
 * <h2>How to use this template</h2>
 *
 * <ol>
 *   <li>Copy this file into your service's {@code src/test/java} directory
 *   <li>Rename the class (e.g., {@code OrderApiContractTest})
 *   <li>Replace sample JSON with actual API response structures
 *   <li>Verify required fields, types, and enum values
 * </ol>
 *
 * <h2>Conventions</h2>
 *
 * <ul>
 *   <li>Extend {@link BaseContractTest} for JSON parsing utilities
 *   <li>Focus on response structure, not business logic
 *   <li>Test both success and error response shapes
 *   <li>Run with unit tests (no Spring context required)
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * class OrderApiContractTest extends BaseContractTest {
 *     @Test
 *     void createOrderResponse_shouldContainOrderIdAndState() {
 *         String json = "{\"orderId\":1,\"state\":\"APPROVED\",\"orderTotal\":\"24.68\"}";
 *         JsonNode node = parseJson(json);
 *         assertFieldExists(node, "orderId");
 *         assertFieldExists(node, "state");
 *         assertFieldExists(node, "orderTotal");
 *     }
 * }
 * }</pre>
 *
 * @see BaseContractTest
 */
@DisplayName("ContractTestTemplate — copy and rename")
@SuppressWarnings(
        "checkstyle:MethodName") // Template uses test naming convention: method_condition_result
public class ContractTestTemplate extends BaseContractTest {

    @Nested
    @DisplayName("Success responses")
    class SuccessResponses {

        @Test
        @DisplayName("response should contain all required fields")
        void response_shouldContainRequiredFields() {
            // Replace with actual API response JSON
            String json = "{\"id\":1,\"name\":\"example\",\"status\":\"ACTIVE\"}";

            JsonNode node = parseJson(json);
            assertFieldExists(node, "id");
            assertFieldExists(node, "name");
            assertFieldExists(node, "status");
        }

        @Test
        @DisplayName("response fields should have correct types")
        void response_shouldHaveCorrectTypes() {
            String json = "{\"id\":1,\"name\":\"example\",\"active\":true}";

            JsonNode node = parseJson(json);
            assertThat(node.get("id").isNumber()).isTrue();
            assertThat(node.get("name").isTextual()).isTrue();
            assertThat(node.get("active").isBoolean()).isTrue();
        }
    }

    @Nested
    @DisplayName("Error responses")
    class ErrorResponses {

        @Test
        @DisplayName("error response should contain message and status")
        void errorResponse_shouldContainMessageAndStatus() {
            String json = "{\"status\":404,\"message\":\"Not found\",\"timestamp\":\"2024-01-01\"}";

            JsonNode node = parseJson(json);
            assertFieldExists(node, "status");
            assertFieldExists(node, "message");
            assertFieldExists(node, "timestamp");
        }
    }
}
