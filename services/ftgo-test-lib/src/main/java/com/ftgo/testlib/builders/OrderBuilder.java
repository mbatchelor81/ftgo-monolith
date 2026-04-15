package com.ftgo.testlib.builders;

import com.ftgo.common.Money;
import com.ftgo.domain.Order;
import com.ftgo.domain.OrderLineItem;
import com.ftgo.domain.Restaurant;
import java.util.List;

/**
 * Fluent builder for creating {@link Order} instances in tests.
 *
 * <p>Provides sensible defaults so tests only need to override fields relevant to the scenario
 * under test.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * var order = OrderBuilder.anOrder()
 *     .withConsumerId(42L)
 *     .build();
 * }</pre>
 */
public final class OrderBuilder {

    private long consumerId = 1L;
    private Restaurant restaurant;
    private List<OrderLineItem> lineItems;

    private OrderBuilder() {
        restaurant = RestaurantBuilder.aRestaurant().build();
        lineItems = List.of(new OrderLineItem("menu-1", "Chicken Vindaloo", new Money("12.34"), 2));
    }

    /** Creates a new builder with sensible defaults. */
    public static OrderBuilder anOrder() {
        return new OrderBuilder();
    }

    /** Sets the consumer ID for the order. */
    public OrderBuilder withConsumerId(long consumerId) {
        this.consumerId = consumerId;
        return this;
    }

    /** Sets the restaurant for the order. */
    public OrderBuilder withRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        return this;
    }

    /** Sets the line items for the order. */
    public OrderBuilder withLineItems(List<OrderLineItem> lineItems) {
        this.lineItems = lineItems;
        return this;
    }

    /** Builds the {@link Order} instance. The order starts in APPROVED state. */
    public Order build() {
        return new Order(consumerId, restaurant, lineItems);
    }
}
