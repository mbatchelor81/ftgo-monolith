package net.chrisrichardson.ftgo.deliveryservice.web;

import net.chrisrichardson.ftgo.deliveryservice.domain.DeliveryAction;
import net.chrisrichardson.ftgo.deliveryservice.domain.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.List;

public class GetDeliveryResponse {
  private Long id;
  private Long orderId;
  private Long courierId;
  private LocalDateTime pickupTime;
  private LocalDateTime dropoffTime;
  private DeliveryStatus status;
  private List<DeliveryAction> actions;

  public GetDeliveryResponse() {
  }

  public GetDeliveryResponse(Long id, Long orderId, Long courierId, LocalDateTime pickupTime,
                             LocalDateTime dropoffTime, DeliveryStatus status, List<DeliveryAction> actions) {
    this.id = id;
    this.orderId = orderId;
    this.courierId = courierId;
    this.pickupTime = pickupTime;
    this.dropoffTime = dropoffTime;
    this.status = status;
    this.actions = actions;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getOrderId() { return orderId; }
  public void setOrderId(Long orderId) { this.orderId = orderId; }
  public Long getCourierId() { return courierId; }
  public void setCourierId(Long courierId) { this.courierId = courierId; }
  public LocalDateTime getPickupTime() { return pickupTime; }
  public void setPickupTime(LocalDateTime pickupTime) { this.pickupTime = pickupTime; }
  public LocalDateTime getDropoffTime() { return dropoffTime; }
  public void setDropoffTime(LocalDateTime dropoffTime) { this.dropoffTime = dropoffTime; }
  public DeliveryStatus getStatus() { return status; }
  public void setStatus(DeliveryStatus status) { this.status = status; }
  public List<DeliveryAction> getActions() { return actions; }
  public void setActions(List<DeliveryAction> actions) { this.actions = actions; }
}
