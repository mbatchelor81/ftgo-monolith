package net.chrisrichardson.ftgo.orderservice.domain;

public class NoCourierAvailableException extends RuntimeException {
  public NoCourierAvailableException() {
    super("No available couriers found");
  }
}
