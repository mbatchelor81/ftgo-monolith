package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumerTest {

  @Test
  void constructor_setsName() {
    PersonName name = new PersonName("John", "Doe");
    Consumer consumer = new Consumer(name);

    assertThat(consumer.getName().getFirstName()).isEqualTo("John");
    assertThat(consumer.getName().getLastName()).isEqualTo("Doe");
  }

  @Test
  void validateOrderByConsumer_doesNotThrow() {
    Consumer consumer = new Consumer(new PersonName("Jane", "Doe"));
    consumer.validateOrderByConsumer(new Money("100.00"));
    // No exception expected — validation is a placeholder
  }
}
