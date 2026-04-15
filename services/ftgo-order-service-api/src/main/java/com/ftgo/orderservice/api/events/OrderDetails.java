package com.ftgo.orderservice.api.events;

import com.ftgo.common.Money;
import java.util.List;

public class OrderDetails {

    private List<OrderLineItemDTO> lineItems;
    private Money orderTotal;
    private long restaurantId;
    private long consumerId;

    private OrderDetails() {}

    public OrderDetails(
            long consumerId,
            long restaurantId,
            List<OrderLineItemDTO> lineItems,
            Money orderTotal) {
        this.consumerId = consumerId;
        this.restaurantId = restaurantId;
        this.lineItems = lineItems;
        this.orderTotal = orderTotal;
    }

    public Money getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(Money orderTotal) {
        this.orderTotal = orderTotal;
    }

    public List<OrderLineItemDTO> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<OrderLineItemDTO> lineItems) {
        this.lineItems = lineItems;
    }

    public long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(long consumerId) {
        this.consumerId = consumerId;
    }
}
