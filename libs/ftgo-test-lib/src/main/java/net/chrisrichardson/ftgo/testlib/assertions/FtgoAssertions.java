package net.chrisrichardson.ftgo.testlib.assertions;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Order;

/**
 * Entry point for FTGO custom AssertJ assertions.
 *
 * <p>Usage:
 * <pre>
 * import static net.chrisrichardson.ftgo.testlib.assertions.FtgoAssertions.assertThat;
 *
 * assertThat(order).hasState(OrderState.APPROVED);
 * assertThat(money).isPositive();
 * </pre>
 */
public final class FtgoAssertions {

    private FtgoAssertions() {
    }

    public static OrderAssert assertThat(Order order) {
        return OrderAssert.assertThat(order);
    }

    public static MoneyAssert assertThat(Money money) {
        return MoneyAssert.assertThat(money);
    }
}
