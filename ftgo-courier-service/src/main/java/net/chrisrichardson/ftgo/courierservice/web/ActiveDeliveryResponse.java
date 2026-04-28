package net.chrisrichardson.ftgo.courierservice.web;

import java.time.LocalDateTime;

public class ActiveDeliveryResponse {
  private Long trackingId;
  private Long orderId;
  private String status;
  private Double distanceKm;
  private LocalDateTime estimatedPickupTime;
  private LocalDateTime estimatedDeliveryTime;

  private ActiveDeliveryResponse() {
  }

  public ActiveDeliveryResponse(Long trackingId, Long orderId, String status,
                                Double distanceKm, LocalDateTime estimatedPickupTime,
                                LocalDateTime estimatedDeliveryTime) {
    this.trackingId = trackingId;
    this.orderId = orderId;
    this.status = status;
    this.distanceKm = distanceKm;
    this.estimatedPickupTime = estimatedPickupTime;
    this.estimatedDeliveryTime = estimatedDeliveryTime;
  }

  public Long getTrackingId() {
    return trackingId;
  }

  public Long getOrderId() {
    return orderId;
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
}
