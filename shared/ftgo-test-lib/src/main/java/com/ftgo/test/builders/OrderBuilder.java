package com.ftgo.test.builders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Test data builder for Order-related test data.
 * Usage: OrderBuilder.anOrder().withConsumerId(1L).withRestaurantId(2L).build()
 */
public class OrderBuilder {

    private Long id;
    private Long consumerId = 1L;
    private Long restaurantId = 1L;
    private List<OrderLineItemData> lineItems = new ArrayList<>();

    private OrderBuilder() {
        lineItems.add(new OrderLineItemData("item-1", "Test Item", new BigDecimal("10.00"), 1));
    }

    public static OrderBuilder anOrder() {
        return new OrderBuilder();
    }

    public OrderBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public OrderBuilder withConsumerId(Long consumerId) {
        this.consumerId = consumerId;
        return this;
    }

    public OrderBuilder withRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
        return this;
    }

    public OrderBuilder withLineItem(String menuItemId, String name, BigDecimal price, int quantity) {
        this.lineItems.add(new OrderLineItemData(menuItemId, name, price, quantity));
        return this;
    }

    public OrderBuilder withEmptyLineItems() {
        this.lineItems.clear();
        return this;
    }

    public Long getId() {
        return id;
    }

    public Long getConsumerId() {
        return consumerId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public List<OrderLineItemData> getLineItems() {
        return List.copyOf(lineItems);
    }

    /**
     * Immutable record representing a line item in a test order.
     */
    public record OrderLineItemData(String menuItemId, String name, BigDecimal price, int quantity) {}
}
