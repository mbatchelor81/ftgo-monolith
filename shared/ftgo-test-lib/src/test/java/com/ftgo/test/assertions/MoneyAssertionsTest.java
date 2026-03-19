package com.ftgo.test.assertions;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyAssertionsTest {

    @Test
    void assertMoneyEquals_withEqualValues() {
        MoneyAssertions.assertMoneyEquals(new BigDecimal("10.00"), new BigDecimal("10.0"));
    }

    @Test
    void assertMoneyEquals_withUnequalValues_throws() {
        assertThatThrownBy(() ->
                MoneyAssertions.assertMoneyEquals(new BigDecimal("10.00"), new BigDecimal("20.00")))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void assertMoneyPositive_withPositiveValue() {
        MoneyAssertions.assertMoneyPositive(new BigDecimal("1.00"));
    }

    @Test
    void assertMoneyZero_withZeroValue() {
        MoneyAssertions.assertMoneyZero(BigDecimal.ZERO);
    }
}
