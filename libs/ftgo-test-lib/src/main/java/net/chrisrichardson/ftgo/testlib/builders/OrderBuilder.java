package net.chrisrichardson.ftgo.testlib.builders;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderLineItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test data builder for {@link Order} entities.
 *
 * <p>Usage:
 * <pre>
 * Order order = OrderBuilder.anOrder()
 *     .withConsumerId(1L)
 *     .withRestaurant(RestaurantBuilder.aRestaurant().build())
 *     .withLineItem("1", "Chicken Vindaloo", new Money("12.34"), 2)
 *     .build();
 * </pre>
 */
public final class OrderBuilder {

    private Long id;
    private long consumerId = 1L;
    private Restaurant restaurant;
    private final List<OrderLineItem> lineItems = new ArrayList<>();

    private OrderBuilder() {
    }

    public static OrderBuilder anOrder() {
        return new OrderBuilder();
    }

    public OrderBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public OrderBuilder withConsumerId(long consumerId) {
        this.consumerId = consumerId;
        return this;
    }

    public OrderBuilder withRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        return this;
    }

    public OrderBuilder withLineItem(String menuItemId, String name, Money price, int quantity) {
        lineItems.add(new OrderLineItem(menuItemId, name, price, quantity));
        return this;
    }

    public Order build() {
        if (restaurant == null) {
            restaurant = RestaurantBuilder.aRestaurant().build();
        }

        List<OrderLineItem> items = lineItems.isEmpty()
                ? Collections.singletonList(new OrderLineItem("1", "Default Item", new Money("10.00"), 1))
                : lineItems;

        Order order = new Order(consumerId, restaurant, items);

        if (id != null) {
            order.setId(id);
        }

        return order;
    }
}
