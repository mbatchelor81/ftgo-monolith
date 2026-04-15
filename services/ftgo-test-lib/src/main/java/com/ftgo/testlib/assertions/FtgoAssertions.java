package com.ftgo.testlib.assertions;

import com.ftgo.common.Money;
import com.ftgo.domain.Order;

/**
 * Entry point for FTGO custom AssertJ assertions.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * import static com.ftgo.testlib.assertions.FtgoAssertions.assertThat;
 *
 * assertThat(order).hasState(OrderState.APPROVED);
 * assertThat(money).isEqualToAmount("12.34");
 * }</pre>
 */
public final class FtgoAssertions {

    private FtgoAssertions() {}

    /** Creates an assertion for {@link Order}. */
    public static OrderAssert assertThat(Order actual) {
        return new OrderAssert(actual);
    }

    /** Creates an assertion for {@link Money}. */
    public static MoneyAssert assertThat(Money actual) {
        return new MoneyAssert(actual);
    }
}
