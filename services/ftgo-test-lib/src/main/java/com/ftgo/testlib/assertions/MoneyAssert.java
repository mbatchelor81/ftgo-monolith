package com.ftgo.testlib.assertions;

import com.ftgo.common.Money;
import org.assertj.core.api.AbstractAssert;

/**
 * Custom AssertJ assertion for {@link Money} value objects.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * assertThat(money).isEqualToAmount("12.34");
 * assertThat(money).isGreaterThanOrEqualTo(new Money(10));
 * }</pre>
 *
 * <p>Import via {@link FtgoAssertions#assertThat(Money)}.
 */
public final class MoneyAssert extends AbstractAssert<MoneyAssert, Money> {

    MoneyAssert(Money actual) {
        super(actual, MoneyAssert.class);
    }

    /** Verifies that the money amount equals the expected string representation. */
    public MoneyAssert isEqualToAmount(String expected) {
        isNotNull();
        String actualAmount = actual.asString();
        if (!actualAmount.equals(expected)) {
            failWithMessage(
                    "Expected money amount to be <%s> but was <%s>", expected, actualAmount);
        }
        return this;
    }

    /** Verifies that this money is greater than or equal to the given amount. */
    public MoneyAssert isGreaterThanOrEqualTo(Money other) {
        isNotNull();
        if (!actual.isGreaterThanOrEqual(other)) {
            failWithMessage("Expected <%s> to be >= <%s>", actual.asString(), other.asString());
        }
        return this;
    }

    /** Verifies that this money equals zero. */
    public MoneyAssert isZero() {
        isNotNull();
        if (!actual.equals(Money.ZERO)) {
            failWithMessage("Expected money to be zero but was <%s>", actual.asString());
        }
        return this;
    }

    /** Verifies that this money is not zero. */
    public MoneyAssert isNotZero() {
        isNotNull();
        if (actual.equals(Money.ZERO)) {
            failWithMessage("Expected money to be non-zero but was zero");
        }
        return this;
    }
}
