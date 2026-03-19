package net.chrisrichardson.ftgo.deliveryservice.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deliveries")
@Access(AccessType.FIELD)
public class Delivery {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long orderId;

  private Long courierId;

  private LocalDateTime pickupTime;

  private LocalDateTime dropoffTime;

  @Enumerated(EnumType.STRING)
  private DeliveryStatus status;

  @ElementCollection
  @CollectionTable(name = "delivery_actions")
  private List<DeliveryAction> actions = new ArrayList<>();

  private Delivery() {
  }

  public Delivery(Long orderId, Long courierId, LocalDateTime pickupTime, LocalDateTime dropoffTime) {
    this.orderId = orderId;
    this.courierId = courierId;
    this.pickupTime = pickupTime;
    this.dropoffTime = dropoffTime;
    this.status = DeliveryStatus.SCHEDULED;
  }

  public void addAction(DeliveryAction action) {
    actions.add(action);
  }

  public Long getId() {
    return id;
  }

  public Long getOrderId() {
    return orderId;
  }

  public Long getCourierId() {
    return courierId;
  }

  public LocalDateTime getPickupTime() {
    return pickupTime;
  }

  public LocalDateTime getDropoffTime() {
    return dropoffTime;
  }

  public DeliveryStatus getStatus() {
    return status;
  }

  public List<DeliveryAction> getActions() {
    return actions;
  }
}
