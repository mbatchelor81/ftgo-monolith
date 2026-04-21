package com.ftgo.test.assertions;

import com.ftgo.test.fixtures.OrderFixture;
import net.chrisrichardson.ftgo.common.Money;
import org.assertj.core.api.AbstractAssert;

/**
 * AssertJ custom assertion for {@link OrderFixture} and other order-shaped
 * domain objects.
 *
 * <pre>{@code
 * OrderAssert.assertThat(order)
 *     .hasState("APPROVED")
 *     .hasTotal(new Money("25.00"))
 *     .belongsToConsumer(42L);
 * }</pre>
 */
public final class OrderAssert extends AbstractAssert<OrderAssert, OrderFixture> {

    private OrderAssert(OrderFixture actual) {
        super(actual, OrderAssert.class);
    }

    public static OrderAssert assertThat(OrderFixture actual) {
        return new OrderAssert(actual);
    }

    public OrderAssert hasState(String expectedState) {
        isNotNull();
        if (!expectedState.equals(actual.state())) {
            failWithMessage("Expected order <%s> to be in state <%s> but was <%s>",
                    actual.id(), expectedState, actual.state());
        }
        return this;
    }

    public OrderAssert hasTotal(Money expected) {
        isNotNull();
        MoneyAssert.assertThat(actual.orderTotal()).isEqualToAmount(expected.asString());
        return this;
    }

    public OrderAssert belongsToConsumer(Long expectedConsumerId) {
        isNotNull();
        if (!expectedConsumerId.equals(actual.consumerId())) {
            failWithMessage("Expected order <%s> to belong to consumer <%s> but was <%s>",
                    actual.id(), expectedConsumerId, actual.consumerId());
        }
        return this;
    }

    public OrderAssert hasLineItemCount(int expected) {
        isNotNull();
        int actualCount = actual.lineItems().size();
        if (actualCount != expected) {
            failWithMessage("Expected order <%s> to have <%d> line items but had <%d>",
                    actual.id(), expected, actualCount);
        }
        return this;
    }
}
