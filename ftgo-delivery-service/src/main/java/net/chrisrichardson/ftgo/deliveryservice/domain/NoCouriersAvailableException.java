package net.chrisrichardson.ftgo.deliveryservice.domain;

public class NoCouriersAvailableException extends RuntimeException {

  public NoCouriersAvailableException() {
    super("No couriers available for delivery");
  }
}
