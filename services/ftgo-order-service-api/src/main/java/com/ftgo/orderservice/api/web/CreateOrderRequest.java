package com.ftgo.orderservice.api.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/** Request DTO for creating a new order. */
public class CreateOrderRequest {

    @Positive(message = "restaurantId must be positive")
    private long restaurantId;

    @Positive(message = "consumerId must be positive")
    private long consumerId;

    @NotEmpty(message = "lineItems must not be empty")
    @Valid
    private List<LineItem> lineItems;

    public CreateOrderRequest(long consumerId, long restaurantId, List<LineItem> lineItems) {
        this.restaurantId = restaurantId;
        this.consumerId = consumerId;
        this.lineItems = lineItems;
    }

    private CreateOrderRequest() {}

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

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    /** Represents a single line item in the order request. */
    public static class LineItem {

        @NotNull(message = "menuItemId must not be null")
        private String menuItemId;

        @Positive(message = "quantity must be positive")
        private int quantity;

        private LineItem() {}

        public LineItem(String menuItemId, int quantity) {
            this.menuItemId = menuItemId;
            this.quantity = quantity;
        }

        public String getMenuItemId() {
            return menuItemId;
        }

        public void setMenuItemId(String menuItemId) {
            this.menuItemId = menuItemId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
