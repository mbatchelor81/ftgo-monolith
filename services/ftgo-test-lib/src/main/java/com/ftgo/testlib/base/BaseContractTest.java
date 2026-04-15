package com.ftgo.testlib.base;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for API contract tests that validate REST response shapes.
 *
 * <p>Contract tests verify that API responses conform to the expected structure (field names,
 * types, required fields) without testing business logic. They sit between unit and integration
 * tests in the testing pyramid.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * class OrderApiContractTest extends BaseContractTest {
 *
 *     @Test
 *     void orderResponse_shouldContainRequiredFields() {
 *         String json = "{\"orderId\":1,\"state\":\"APPROVED\"}";
 *         JsonNode node = parseJson(json);
 *         assertFieldExists(node, "orderId");
 *         assertFieldExists(node, "state");
 *     }
 * }
 * }</pre>
 */
public abstract class BaseContractTest {

    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUpObjectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    /**
     * Parses a JSON string into a {@link JsonNode}.
     *
     * @param json the JSON string
     * @return the parsed JSON tree
     */
    protected JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new AssertionError("Failed to parse JSON: " + json, e);
        }
    }

    /**
     * Asserts that a required field exists in the JSON node.
     *
     * @param node the JSON node to inspect
     * @param fieldName the expected field name
     */
    protected void assertFieldExists(JsonNode node, String fieldName) {
        assertThat(node.has(fieldName))
                .as("Expected field '%s' to exist in response", fieldName)
                .isTrue();
    }

    /**
     * Asserts that a field exists and has a non-null value.
     *
     * @param node the JSON node to inspect
     * @param fieldName the expected field name
     */
    protected void assertFieldNotNull(JsonNode node, String fieldName) {
        assertFieldExists(node, fieldName);
        assertThat(node.get(fieldName).isNull())
                .as("Expected field '%s' to be non-null", fieldName)
                .isFalse();
    }

    /**
     * Asserts that a field has the expected string value.
     *
     * @param node the JSON node to inspect
     * @param fieldName the field name
     * @param expected the expected value
     */
    protected void assertFieldEquals(JsonNode node, String fieldName, String expected) {
        assertFieldExists(node, fieldName);
        assertThat(node.get(fieldName).asText())
                .as("Expected field '%s' to equal '%s'", fieldName, expected)
                .isEqualTo(expected);
    }
}
