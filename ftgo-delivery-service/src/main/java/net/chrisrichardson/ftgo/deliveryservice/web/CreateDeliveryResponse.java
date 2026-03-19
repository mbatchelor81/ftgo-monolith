package net.chrisrichardson.ftgo.deliveryservice.web;

public class CreateDeliveryResponse {
  private Long deliveryId;
  private Long courierId;

  public CreateDeliveryResponse() {
  }

  public CreateDeliveryResponse(Long deliveryId, Long courierId) {
    this.deliveryId = deliveryId;
    this.courierId = courierId;
  }

  public Long getDeliveryId() {
    return deliveryId;
  }

  public void setDeliveryId(Long deliveryId) {
    this.deliveryId = deliveryId;
  }

  public Long getCourierId() {
    return courierId;
  }

  public void setCourierId(Long courierId) {
    this.courierId = courierId;
  }
}
