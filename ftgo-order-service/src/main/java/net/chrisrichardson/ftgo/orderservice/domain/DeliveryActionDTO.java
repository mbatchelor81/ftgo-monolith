package net.chrisrichardson.ftgo.orderservice.domain;

import java.time.LocalDateTime;

public class DeliveryActionDTO {
  private String type;
  private LocalDateTime time;
  private Long orderId;

  public DeliveryActionDTO() {
  }

  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public LocalDateTime getTime() { return time; }
  public void setTime(LocalDateTime time) { this.time = time; }
  public Long getOrderId() { return orderId; }
  public void setOrderId(Long orderId) { this.orderId = orderId; }
}
