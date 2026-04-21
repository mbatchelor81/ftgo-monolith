package com.ftgo.test.assertions;

import net.chrisrichardson.ftgo.common.Money;
import org.assertj.core.api.AbstractAssert;

import java.math.BigDecimal;

/**
 * AssertJ custom assertion for {@link Money}.
 *
 * <p>Lets tests express value-object expectations without leaking
 * {@code BigDecimal} comparisons into the assertion site:
 *
 * <pre>{@code
 * assertThat(orderTotal).isEqualToAmount("62.50");
 * assertThat(total).isGreaterThanOrEqualTo(MoneyBuilder.aMoney().withAmount("10.00").build());
 * assertThat(total).isZero();
 * }</pre>
 */
public final class MoneyAssert extends AbstractAssert<MoneyAssert, Money> {

    private MoneyAssert(Money actual) {
        super(actual, MoneyAssert.class);
    }

    public static MoneyAssert assertThat(Money actual) {
        return new MoneyAssert(actual);
    }

    public MoneyAssert isEqualToAmount(String expected) {
        isNotNull();
        BigDecimal actualAmount = new BigDecimal(actual.asString());
        BigDecimal expectedAmount = new BigDecimal(expected);
        if (actualAmount.compareTo(expectedAmount) != 0) {
            failWithMessage("Expected money to have amount <%s> but was <%s>", expected, actual.asString());
        }
        return this;
    }

    public MoneyAssert isGreaterThanOrEqualTo(Money other) {
        isNotNull();
        if (!actual.isGreaterThanOrEqual(other)) {
            failWithMessage("Expected <%s> to be greater than or equal to <%s>", actual.asString(), other.asString());
        }
        return this;
    }

    public MoneyAssert isZero() {
        isNotNull();
        BigDecimal actualAmount = new BigDecimal(actual.asString());
        if (actualAmount.compareTo(BigDecimal.ZERO) != 0) {
            failWithMessage("Expected money to be zero but was <%s>", actual.asString());
        }
        return this;
    }

    public MoneyAssert isPositive() {
        isNotNull();
        BigDecimal actualAmount = new BigDecimal(actual.asString());
        if (actualAmount.signum() <= 0) {
            failWithMessage("Expected money to be positive but was <%s>", actual.asString());
        }
        return this;
    }
}
