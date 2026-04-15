package com.ftgo.orderservice.api.web;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/** DTO representing an order acceptance by a restaurant. */
public class OrderAcceptance {

    @NotNull(message = "readyBy must not be null")
    private LocalDateTime readyBy;

    public OrderAcceptance() {}

    public OrderAcceptance(LocalDateTime readyBy) {
        this.readyBy = readyBy;
    }

    public LocalDateTime getReadyBy() {
        return readyBy;
    }

    public void setReadyBy(LocalDateTime readyBy) {
        this.readyBy = readyBy;
    }
}
