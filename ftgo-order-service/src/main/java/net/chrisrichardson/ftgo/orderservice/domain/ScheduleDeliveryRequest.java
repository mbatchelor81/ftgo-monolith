package net.chrisrichardson.ftgo.orderservice.domain;

import java.time.LocalDateTime;

public class ScheduleDeliveryRequest {
  private Long orderId;
  private LocalDateTime readyBy;

  public ScheduleDeliveryRequest() {
  }

  public ScheduleDeliveryRequest(Long orderId, LocalDateTime readyBy) {
    this.orderId = orderId;
    this.readyBy = readyBy;
  }

  public Long getOrderId() { return orderId; }
  public void setOrderId(Long orderId) { this.orderId = orderId; }
  public LocalDateTime getReadyBy() { return readyBy; }
  public void setReadyBy(LocalDateTime readyBy) { this.readyBy = readyBy; }
}
