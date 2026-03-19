package net.chrisrichardson.ftgo.deliveryservice.web;

import java.time.LocalDateTime;

public class CreateDeliveryRequest {
  private Long orderId;
  private LocalDateTime readyBy;

  public CreateDeliveryRequest() {
  }

  public CreateDeliveryRequest(Long orderId, LocalDateTime readyBy) {
    this.orderId = orderId;
    this.readyBy = readyBy;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public LocalDateTime getReadyBy() {
    return readyBy;
  }

  public void setReadyBy(LocalDateTime readyBy) {
    this.readyBy = readyBy;
  }
}
