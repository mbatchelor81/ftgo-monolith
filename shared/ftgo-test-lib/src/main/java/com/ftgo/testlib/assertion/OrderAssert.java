package com.ftgo.testlib.assertion;

import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderState;
import org.assertj.core.api.AbstractAssert;

/**
 * Custom AssertJ assertion for {@link Order} domain objects.
 *
 * <pre>{@code
 * import static com.ftgo.testlib.assertion.FtgoAssertions.assertThat;
 *
 * assertThat(order)
 *     .hasState(OrderState.APPROVED)
 *     .belongsToConsumer(42L);
 * }</pre>
 */
public class OrderAssert extends AbstractAssert<OrderAssert, Order> {

    public OrderAssert(Order actual) {
        super(actual, OrderAssert.class);
    }

    public static OrderAssert assertThat(Order actual) {
        return new OrderAssert(actual);
    }

    public OrderAssert hasState(OrderState expectedState) {
        isNotNull();
        if (actual.getOrderState() != expectedState) {
            failWithMessage("Expected order state to be <%s> but was <%s>",
                    expectedState, actual.getOrderState());
        }
        return this;
    }

    public OrderAssert isApproved() {
        return hasState(OrderState.APPROVED);
    }

    public OrderAssert isCancelled() {
        return hasState(OrderState.CANCELLED);
    }

    public OrderAssert isAccepted() {
        return hasState(OrderState.ACCEPTED);
    }

    public OrderAssert isPreparing() {
        return hasState(OrderState.PREPARING);
    }

    public OrderAssert isReadyForPickup() {
        return hasState(OrderState.READY_FOR_PICKUP);
    }

    public OrderAssert isPickedUp() {
        return hasState(OrderState.PICKED_UP);
    }

    public OrderAssert isDelivered() {
        return hasState(OrderState.DELIVERED);
    }

    public OrderAssert belongsToConsumer(long consumerId) {
        isNotNull();
        if (!actual.getConsumerId().equals(consumerId)) {
            failWithMessage("Expected order to belong to consumer <%d> but belongs to <%d>",
                    consumerId, actual.getConsumerId());
        }
        return this;
    }

    public OrderAssert hasLineItemCount(int expectedCount) {
        isNotNull();
        int actualCount = actual.getLineItems().size();
        if (actualCount != expectedCount) {
            failWithMessage("Expected order to have <%d> line items but had <%d>",
                    expectedCount, actualCount);
        }
        return this;
    }
}
