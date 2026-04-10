package com.ftgo.order.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Request DTO for creating a new order.
 */
public class CreateOrderRequest {

    @NotNull(message = "Consumer ID is required")
    @Min(value = 1, message = "Consumer ID must be positive")
    private Long consumerId;

    @NotNull(message = "Restaurant ID is required")
    @Min(value = 1, message = "Restaurant ID must be positive")
    private Long restaurantId;

    @NotNull(message = "Line items are required")
    @Size(min = 1, message = "At least one line item is required")
    private Map<@NotBlank(message = "Menu item ID must not be blank") String,
               @NotNull @Min(value = 1, message = "Quantity must be at least 1") Integer> lineItems;

    public Long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(Long consumerId) {
        this.consumerId = consumerId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Map<String, Integer> getLineItems() {
        return lineItems;
    }

    public void setLineItems(Map<String, Integer> lineItems) {
        this.lineItems = lineItems;
    }
}
