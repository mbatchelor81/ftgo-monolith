package com.ftgo.consumer.api;

public class CreateConsumerResponse {

  private long consumerId;

  private CreateConsumerResponse() {
  }

  public CreateConsumerResponse(long consumerId) {
    this.consumerId = consumerId;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(long consumerId) {
    this.consumerId = consumerId;
  }
}
