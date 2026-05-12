package com.ftgo.courier.api;

public class CreateCourierResponse {

  private long courierId;

  private CreateCourierResponse() {
  }

  public CreateCourierResponse(long courierId) {
    this.courierId = courierId;
  }

  public long getCourierId() {
    return courierId;
  }

  public void setCourierId(long courierId) {
    this.courierId = courierId;
  }
}
