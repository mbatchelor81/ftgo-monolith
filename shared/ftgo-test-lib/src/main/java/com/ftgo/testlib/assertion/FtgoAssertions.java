package com.ftgo.testlib.assertion;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Order;

/**
 * Entry point for FTGO custom AssertJ assertions.
 *
 * <pre>{@code
 * import static com.ftgo.testlib.assertion.FtgoAssertions.assertThat;
 *
 * assertThat(order).isApproved().belongsToConsumer(1L);
 * assertThat(money).hasAmount("25.98");
 * }</pre>
 */
public final class FtgoAssertions {

    private FtgoAssertions() {
    }

    public static OrderAssert assertThat(Order order) {
        return new OrderAssert(order);
    }

    public static MoneyAssert assertThat(Money money) {
        return new MoneyAssert(money);
    }
}
