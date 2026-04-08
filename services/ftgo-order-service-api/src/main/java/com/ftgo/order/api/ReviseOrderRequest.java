package com.ftgo.order.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Request DTO for revising an existing order's line items.
 */
public class ReviseOrderRequest {

    @NotNull(message = "Revised line items are required")
    @Size(min = 1, message = "At least one revised line item is required")
    private Map<@NotBlank(message = "Menu item ID must not be blank") String,
               @NotNull @Min(value = 0, message = "Quantity must not be negative") Integer> revisedLineItemQuantities;

    public Map<String, Integer> getRevisedLineItemQuantities() {
        return revisedLineItemQuantities;
    }

    public void setRevisedLineItemQuantities(Map<String, Integer> revisedLineItemQuantities) {
        this.revisedLineItemQuantities = revisedLineItemQuantities;
    }
}
