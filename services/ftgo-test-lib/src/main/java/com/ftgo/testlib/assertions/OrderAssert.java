package com.ftgo.testlib.assertions;

import com.ftgo.domain.Order;
import com.ftgo.domain.OrderState;
import org.assertj.core.api.AbstractAssert;

/**
 * Custom AssertJ assertion for {@link Order} entities.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * assertThat(order).hasState(OrderState.APPROVED);
 * assertThat(order).hasConsumerId(42L);
 * }</pre>
 *
 * <p>Import via {@link FtgoAssertions#assertThat(Order)}.
 */
public final class OrderAssert extends AbstractAssert<OrderAssert, Order> {

    OrderAssert(Order actual) {
        super(actual, OrderAssert.class);
    }

    /** Verifies that the order is in the expected state. */
    public OrderAssert hasState(OrderState expected) {
        isNotNull();
        if (actual.getOrderState() != expected) {
            failWithMessage(
                    "Expected order state to be <%s> but was <%s>",
                    expected, actual.getOrderState());
        }
        return this;
    }

    /** Verifies that the order belongs to the expected consumer. */
    public OrderAssert hasConsumerId(long expected) {
        isNotNull();
        if (!actual.getConsumerId().equals(expected)) {
            failWithMessage(
                    "Expected consumer ID to be <%d> but was <%d>",
                    expected, actual.getConsumerId());
        }
        return this;
    }

    /** Verifies that the order has the expected number of line items. */
    public OrderAssert hasLineItemCount(int expected) {
        isNotNull();
        int actualCount = actual.getLineItems().size();
        if (actualCount != expected) {
            failWithMessage("Expected <%d> line items but found <%d>", expected, actualCount);
        }
        return this;
    }

    /** Verifies that the order has an assigned courier. */
    public OrderAssert hasAssignedCourier() {
        isNotNull();
        if (actual.getAssignedCourier() == null) {
            failWithMessage("Expected order to have an assigned courier but it did not");
        }
        return this;
    }

    /** Verifies that the order does not have an assigned courier. */
    public OrderAssert hasNoAssignedCourier() {
        isNotNull();
        if (actual.getAssignedCourier() != null) {
            failWithMessage("Expected order to have no assigned courier but it did");
        }
        return this;
    }
}
