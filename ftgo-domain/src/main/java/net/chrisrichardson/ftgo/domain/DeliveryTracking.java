package net.chrisrichardson.ftgo.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_tracking")
@Access(AccessType.FIELD)
public class DeliveryTracking {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "courier_id", nullable = false)
  private Courier courier;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private DeliveryStatus status;

  private LocalDateTime estimatedPickupTime;

  private LocalDateTime estimatedDeliveryTime;

  @Column(name = "distance_km")
  private Double distanceKm;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  private DeliveryTracking() {
  }

  public DeliveryTracking(Order order, Courier courier, double distanceKm,
                          LocalDateTime estimatedPickupTime,
                          LocalDateTime estimatedDeliveryTime) {
    this.order = order;
    this.courier = courier;
    this.status = DeliveryStatus.ASSIGNED;
    this.distanceKm = distanceKm;
    this.estimatedPickupTime = estimatedPickupTime;
    this.estimatedDeliveryTime = estimatedDeliveryTime;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public Order getOrder() {
    return order;
  }

  public Courier getCourier() {
    return courier;
  }

  public DeliveryStatus getStatus() {
    return status;
  }

  public LocalDateTime getEstimatedPickupTime() {
    return estimatedPickupTime;
  }

  public LocalDateTime getEstimatedDeliveryTime() {
    return estimatedDeliveryTime;
  }

  public Double getDistanceKm() {
    return distanceKm;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void updateStatus(DeliveryStatus newStatus) {
    this.status = newStatus;
    this.updatedAt = LocalDateTime.now();
  }
}
