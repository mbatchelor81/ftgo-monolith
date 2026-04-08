package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourierTest {

  @Test
  void noteAvailable_setsAvailableTrue() {
    Courier courier = new Courier(new PersonName("Alice", "Smith"),
        new Address("456 Elm St", null, "Chicago", "IL", "60601"));
    courier.noteAvailable();

    assertThat(courier.isAvailable()).isTrue();
  }

  @Test
  void noteUnavailable_setsAvailableFalse() {
    Courier courier = new Courier(new PersonName("Bob", "Jones"),
        new Address("789 Oak St", null, "Denver", "CO", "80201"));
    courier.noteAvailable();
    courier.noteUnavailable();

    assertThat(courier.isAvailable()).isFalse();
  }
}
