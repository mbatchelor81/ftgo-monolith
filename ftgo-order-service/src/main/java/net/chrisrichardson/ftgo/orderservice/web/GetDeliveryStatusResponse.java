package net.chrisrichardson.ftgo.orderservice.web;

import java.time.LocalDateTime;

public class GetDeliveryStatusResponse {
  private Long trackingId;
  private Long orderId;
  private Long courierId;
  private String status;
  private Double distanceKm;
  private LocalDateTime estimatedPickupTime;
  private LocalDateTime estimatedDeliveryTime;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private GetDeliveryStatusResponse() {
  }

  public GetDeliveryStatusResponse(Long trackingId, Long orderId, Long courierId,
                                    String status, Double distanceKm,
                                    LocalDateTime estimatedPickupTime,
                                    LocalDateTime estimatedDeliveryTime,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.trackingId = trackingId;
    this.orderId = orderId;
    this.courierId = courierId;
    this.status = status;
    this.distanceKm = distanceKm;
    this.estimatedPickupTime = estimatedPickupTime;
    this.estimatedDeliveryTime = estimatedDeliveryTime;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getTrackingId() {
    return trackingId;
  }

  public Long getOrderId() {
    return orderId;
  }

  public Long getCourierId() {
    return courierId;
  }

  public String getStatus() {
    return status;
  }

  public Double getDistanceKm() {
    return distanceKm;
  }

  public LocalDateTime getEstimatedPickupTime() {
    return estimatedPickupTime;
  }

  public LocalDateTime getEstimatedDeliveryTime() {
    return estimatedDeliveryTime;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
