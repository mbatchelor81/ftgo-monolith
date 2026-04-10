package com.ftgo.testlib.builder;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderLineItem;
import net.chrisrichardson.ftgo.domain.Restaurant;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link Order} test instances.
 *
 * <pre>{@code
 * Order order = OrderBuilder.order()
 *     .withConsumerId(1L)
 *     .withLineItem("1", "Chicken Vindaloo", "12.99", 2)
 *     .build();
 * }</pre>
 */
public final class OrderBuilder {

    private long consumerId = 1L;
    private Restaurant restaurant;
    private final List<OrderLineItem> lineItems = new ArrayList<>();

    private OrderBuilder() {
        restaurant = RestaurantBuilder.restaurant().withId(1L).build();
        lineItems.add(new OrderLineItem("menu-item-1", "Chicken Vindaloo", new Money("12.99"), 2));
    }

    public static OrderBuilder order() {
        return new OrderBuilder();
    }

    public OrderBuilder withConsumerId(long consumerId) {
        this.consumerId = consumerId;
        return this;
    }

    public OrderBuilder withRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        return this;
    }

    public OrderBuilder withLineItem(String menuItemId, String name, String price, int quantity) {
        this.lineItems.add(new OrderLineItem(menuItemId, name, new Money(price), quantity));
        return this;
    }

    public OrderBuilder withLineItems(List<OrderLineItem> lineItems) {
        this.lineItems.clear();
        this.lineItems.addAll(lineItems);
        return this;
    }

    public OrderBuilder clearLineItems() {
        this.lineItems.clear();
        return this;
    }

    public Order build() {
        return new Order(consumerId, restaurant, lineItems);
    }
}
