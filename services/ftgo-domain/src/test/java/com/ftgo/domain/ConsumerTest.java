package com.ftgo.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.ftgo.common.Money;
import com.ftgo.common.PersonName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Consumer} entity.
 *
 * <p>Demonstrates testing of:
 *
 * <ul>
 *   <li>Entity construction with value objects
 *   <li>Order validation logic
 * </ul>
 */
@DisplayName("Consumer")
class ConsumerTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should create consumer with person name")
        void constructor_withPersonName_createsConsumer() {
            var name = new PersonName("John", "Doe");
            var consumer = new Consumer(name);

            assertThat(consumer.getName()).isNotNull();
            assertThat(consumer.getName().getFirstName()).isEqualTo("John");
            assertThat(consumer.getName().getLastName()).isEqualTo("Doe");
        }
    }

    @Nested
    @DisplayName("validateOrderByConsumer")
    class ValidateOrderByConsumer {

        @Test
        @DisplayName("should accept valid order total without exception")
        void validateOrderByConsumer_withValidTotal_doesNotThrow() {
            var consumer = new Consumer(new PersonName("Jane", "Doe"));
            // Currently a no-op; this test documents the expected behavior
            consumer.validateOrderByConsumer(new Money(100));
        }
    }
}
