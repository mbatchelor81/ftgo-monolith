package com.ftgo.order.api;

import java.time.LocalDateTime;

public class OrderAcceptance {

  private LocalDateTime readyBy;

  private OrderAcceptance() {
  }

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
