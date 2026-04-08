package com.ftgo.testlib.assertion;

import net.chrisrichardson.ftgo.common.Money;
import org.assertj.core.api.AbstractAssert;

/**
 * Custom AssertJ assertion for {@link Money} value objects.
 *
 * <pre>{@code
 * import static com.ftgo.testlib.assertion.FtgoAssertions.assertThat;
 *
 * assertThat(orderTotal)
 *     .isGreaterThanOrEqualTo(new Money("25.00"))
 *     .isNotZero();
 * }</pre>
 */
public class MoneyAssert extends AbstractAssert<MoneyAssert, Money> {

    public MoneyAssert(Money actual) {
        super(actual, MoneyAssert.class);
    }

    public static MoneyAssert assertThat(Money actual) {
        return new MoneyAssert(actual);
    }

    public MoneyAssert isZero() {
        isNotNull();
        if (!actual.equals(Money.ZERO)) {
            failWithMessage("Expected money to be zero but was <%s>", actual.asString());
        }
        return this;
    }

    public MoneyAssert isNotZero() {
        isNotNull();
        if (actual.equals(Money.ZERO)) {
            failWithMessage("Expected money to be non-zero");
        }
        return this;
    }

    public MoneyAssert isGreaterThanOrEqualTo(Money expected) {
        isNotNull();
        if (!actual.isGreaterThanOrEqual(expected)) {
            failWithMessage("Expected money <%s> to be >= <%s>",
                    actual.asString(), expected.asString());
        }
        return this;
    }

    public MoneyAssert hasAmount(String expectedAmount) {
        isNotNull();
        Money expected = new Money(expectedAmount);
        if (!actual.equals(expected)) {
            failWithMessage("Expected money amount <%s> but was <%s>",
                    expectedAmount, actual.asString());
        }
        return this;
    }
}
