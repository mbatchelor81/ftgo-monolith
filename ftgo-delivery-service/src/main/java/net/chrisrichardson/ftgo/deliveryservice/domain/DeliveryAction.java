package net.chrisrichardson.ftgo.deliveryservice.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Embeddable
public class DeliveryAction {

  @Enumerated(EnumType.STRING)
  private ActionType type;
  private LocalDateTime time;
  private Long orderId;

  private DeliveryAction() {
  }

  public DeliveryAction(ActionType type, Long orderId, LocalDateTime time) {
    this.type = type;
    this.orderId = orderId;
    this.time = time;
  }

  public boolean actionForOrder(Long orderId) {
    return this.orderId.equals(orderId);
  }

  public static DeliveryAction makePickup(Long orderId) {
    return new DeliveryAction(ActionType.PICKUP, orderId, null);
  }

  public static DeliveryAction makeDropoff(Long orderId, LocalDateTime deliveryTime) {
    return new DeliveryAction(ActionType.DROPOFF, orderId, deliveryTime);
  }

  public ActionType getType() {
    return type;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public Long getOrderId() {
    return orderId;
  }
}
