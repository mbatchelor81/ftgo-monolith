package net.chrisrichardson.ftgo.testlib.assertions;

import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderState;
import org.assertj.core.api.AbstractAssert;

/**
 * Custom AssertJ assertion for {@link Order} entities.
 *
 * <p>Usage:
 * <pre>
 * import static net.chrisrichardson.ftgo.testlib.assertions.FtgoAssertions.assertThat;
 *
 * assertThat(order)
 *     .hasState(OrderState.APPROVED)
 *     .hasConsumerId(1L);
 * </pre>
 */
public class OrderAssert extends AbstractAssert<OrderAssert, Order> {

    protected OrderAssert(Order actual) {
        super(actual, OrderAssert.class);
    }

    public static OrderAssert assertThat(Order actual) {
        return new OrderAssert(actual);
    }

    public OrderAssert hasState(OrderState expected) {
        isNotNull();
        if (actual.getOrderState() != expected) {
            failWithMessage("Expected order state to be <%s> but was <%s>",
                    expected, actual.getOrderState());
        }
        return this;
    }

    public OrderAssert hasConsumerId(long expected) {
        isNotNull();
        if (actual.getConsumerId() != expected) {
            failWithMessage("Expected consumer ID to be <%d> but was <%d>",
                    expected, actual.getConsumerId());
        }
        return this;
    }

    public OrderAssert isApproved() {
        return hasState(OrderState.APPROVED);
    }

    public OrderAssert isCancelled() {
        return hasState(OrderState.CANCELLED);
    }
}
