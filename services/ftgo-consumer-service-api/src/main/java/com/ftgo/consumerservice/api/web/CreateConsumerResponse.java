package com.ftgo.consumerservice.api.web;

/** Response DTO returned after creating a consumer. */
public class CreateConsumerResponse {

    private long consumerId;

    public CreateConsumerResponse() {}

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
