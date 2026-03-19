package com.ftgo.test.assertions;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Custom AssertJ-style assertions for Money/BigDecimal comparisons in tests.
 * Handles BigDecimal scale differences (e.g., 10.00 vs 10.0).
 */
public final class MoneyAssertions {

    private MoneyAssertions() {
        // utility class
    }

    /**
     * Assert that two BigDecimal values are equal by numeric value (ignoring scale).
     */
    public static void assertMoneyEquals(BigDecimal expected, BigDecimal actual) {
        assertThat(actual)
                .as("Expected money value %s but was %s", expected, actual)
                .isNotNull()
                .isEqualByComparingTo(expected);
    }

    /**
     * Assert that a BigDecimal value is positive.
     */
    public static void assertMoneyPositive(BigDecimal actual) {
        assertThat(actual)
                .as("Expected positive money value but was %s", actual)
                .isNotNull()
                .isGreaterThan(BigDecimal.ZERO);
    }

    /**
     * Assert that a BigDecimal value is zero.
     */
    public static void assertMoneyZero(BigDecimal actual) {
        assertThat(actual)
                .as("Expected zero money value but was %s", actual)
                .isNotNull()
                .isEqualByComparingTo(BigDecimal.ZERO);
    }
}
