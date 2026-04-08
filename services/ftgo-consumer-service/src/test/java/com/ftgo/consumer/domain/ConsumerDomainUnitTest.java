package com.ftgo.consumer.domain;

import com.ftgo.testlib.base.BaseUnitTest;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ftgo.testlib.builder.TestBuilders.consumer;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test template for Consumer domain logic.
 * Demonstrates testing entity creation and validation using test builders.
 */
@DisplayName("Consumer Domain")
class ConsumerDomainUnitTest extends BaseUnitTest {

    @Nested
    @DisplayName("Consumer creation")
    class Creation {

        @Test
        void createConsumer_withValidName_setsNameCorrectly() {
            // Arrange & Act
            Consumer consumer = consumer()
                    .withFirstName("Jane")
                    .withLastName("Smith")
                    .build();

            // Assert
            assertThat(consumer.getName().getFirstName()).isEqualTo("Jane");
            assertThat(consumer.getName().getLastName()).isEqualTo("Smith");
        }

        @Test
        void createConsumer_withDefaults_usesDefaultValues() {
            // Arrange & Act
            Consumer consumer = consumer().build();

            // Assert
            assertThat(consumer.getName()).isNotNull();
            assertThat(consumer.getName().getFirstName()).isEqualTo("John");
            assertThat(consumer.getName().getLastName()).isEqualTo("Doe");
        }
    }

    @Nested
    @DisplayName("Order validation")
    class OrderValidation {

        @Test
        void validateOrderByConsumer_withValidAmount_doesNotThrow() {
            // Arrange
            Consumer consumer = consumer().build();
            Money orderTotal = new Money("25.00");

            // Act & Assert — no exception means validation passed
            consumer.validateOrderByConsumer(orderTotal);
        }
    }
}
