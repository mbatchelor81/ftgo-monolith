package com.ftgo.courier.api;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating courier availability.
 */
public class UpdateAvailabilityRequest {

    @NotNull(message = "Available flag is required")
    private Boolean available;

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
