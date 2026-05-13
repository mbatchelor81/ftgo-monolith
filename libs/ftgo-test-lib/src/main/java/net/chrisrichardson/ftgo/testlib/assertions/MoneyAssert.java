package net.chrisrichardson.ftgo.testlib.assertions;

import net.chrisrichardson.ftgo.common.Money;
import org.assertj.core.api.AbstractAssert;

/**
 * Custom AssertJ assertion for {@link Money} value objects.
 *
 * <p>Usage:
 * <pre>
 * import static net.chrisrichardson.ftgo.testlib.assertions.FtgoAssertions.assertThat;
 *
 * assertThat(order.getOrderTotal())
 *     .isPositive()
 *     .isGreaterThanOrEqualTo(new Money("10.00"));
 * </pre>
 */
public class MoneyAssert extends AbstractAssert<MoneyAssert, Money> {

    protected MoneyAssert(Money actual) {
        super(actual, MoneyAssert.class);
    }

    public static MoneyAssert assertThat(Money actual) {
        return new MoneyAssert(actual);
    }

    public MoneyAssert isEqualTo(String expected) {
        isNotNull();
        Money expectedMoney = new Money(expected);
        if (!actual.equals(expectedMoney)) {
            failWithMessage("Expected money to be <%s> but was <%s>",
                    expectedMoney.asString(), actual.asString());
        }
        return this;
    }

    public MoneyAssert isZero() {
        isNotNull();
        if (!actual.equals(Money.ZERO)) {
            failWithMessage("Expected money to be zero but was <%s>", actual.asString());
        }
        return this;
    }

    public MoneyAssert isPositive() {
        isNotNull();
        if (!actual.isGreaterThanOrEqual(new Money("0.01"))) {
            failWithMessage("Expected money to be positive but was <%s>", actual.asString());
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
}
