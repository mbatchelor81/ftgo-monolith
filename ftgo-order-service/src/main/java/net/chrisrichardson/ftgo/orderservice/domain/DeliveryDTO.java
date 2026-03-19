package net.chrisrichardson.ftgo.orderservice.domain;

import java.time.LocalDateTime;
import java.util.List;

public class DeliveryDTO {
  private Long id;
  private Long orderId;
  private Long courierId;
  private LocalDateTime pickupTime;
  private LocalDateTime dropoffTime;
  private String status;
  private List<DeliveryActionDTO> actions;

  public DeliveryDTO() {
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
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public List<DeliveryActionDTO> getActions() { return actions; }
  public void setActions(List<DeliveryActionDTO> actions) { this.actions = actions; }
}
