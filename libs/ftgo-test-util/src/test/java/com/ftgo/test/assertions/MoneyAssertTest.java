package com.ftgo.test.assertions;

import com.ftgo.test.builders.MoneyBuilder;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.jupiter.api.Test;

import static com.ftgo.test.assertions.MoneyAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyAssertTest {

    @Test
    void isEqualToAmount_withMatchingAmount_passes() {
        Money amount = MoneyBuilder.aMoney().withAmount("12.50").build();

        assertThat(amount).isEqualToAmount("12.50");
    }

    @Test
    void isEqualToAmount_normalizesScale() {
        Money amount = MoneyBuilder.aMoney().withAmount("12.5").build();

        // Scale differs but numeric value matches — the assertion should pass.
        assertThat(amount).isEqualToAmount("12.50");
    }

    @Test
    void isEqualToAmount_withMismatchedAmount_fails() {
        Money amount = MoneyBuilder.aMoney().withAmount("12.50").build();

        assertThatThrownBy(() -> assertThat(amount).isEqualToAmount("99.99"))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void isZero_withZeroAmount_passes() {
        assertThat(MoneyBuilder.zero()).isZero();
    }

    @Test
    void isZero_withNonZeroAmount_fails() {
        Money amount = MoneyBuilder.aMoney().withAmount("0.01").build();

        assertThatThrownBy(() -> assertThat(amount).isZero())
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void isPositive_withPositiveAmount_passes() {
        assertThat(MoneyBuilder.aMoney().withAmount("1.00").build()).isPositive();
    }

    @Test
    void isPositive_withZeroAmount_fails() {
        assertThatThrownBy(() -> assertThat(MoneyBuilder.zero()).isPositive())
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void isGreaterThanOrEqualTo_whenGreater_passes() {
        Money larger = MoneyBuilder.aMoney().withAmount("100").build();
        Money smaller = MoneyBuilder.aMoney().withAmount("10").build();

        assertThat(larger).isGreaterThanOrEqualTo(smaller);
    }

    @Test
    void isGreaterThanOrEqualTo_whenSmaller_fails() {
        Money smaller = MoneyBuilder.aMoney().withAmount("5").build();
        Money larger = MoneyBuilder.aMoney().withAmount("50").build();

        assertThatThrownBy(() -> assertThat(smaller).isGreaterThanOrEqualTo(larger))
                .isInstanceOf(AssertionError.class);
    }
}
