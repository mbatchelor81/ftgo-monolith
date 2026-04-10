package com.ftgo.courier.domain;

import com.ftgo.testlib.base.BaseUnitTest;
import net.chrisrichardson.ftgo.domain.Courier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ftgo.testlib.builder.TestBuilders.courier;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test template for Courier domain logic.
 * Demonstrates testing entity creation, availability toggling,
 * and plan management using test builders.
 */
@DisplayName("Courier Domain")
class CourierDomainUnitTest extends BaseUnitTest {

    @Nested
    @DisplayName("Courier creation")
    class Creation {

        @Test
        void createCourier_withDefaults_isAvailable() {
            // Arrange & Act
            Courier courier = courier().build();

            // Assert
            assertThat(courier.isAvailable()).isTrue();
        }

        @Test
        void createCourier_unavailable_isNotAvailable() {
            // Arrange & Act
            Courier courier = courier()
                    .unavailable()
                    .build();

            // Assert
            assertThat(courier.isAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("Availability management")
    class Availability {

        @Test
        void noteAvailable_setsAvailableToTrue() {
            // Arrange
            Courier courier = courier().unavailable().build();

            // Act
            courier.noteAvailable();

            // Assert
            assertThat(courier.isAvailable()).isTrue();
        }

        @Test
        void noteUnavailable_setsAvailableToFalse() {
            // Arrange
            Courier courier = courier().available().build();

            // Act
            courier.noteUnavailable();

            // Assert
            assertThat(courier.isAvailable()).isFalse();
        }
    }
}
