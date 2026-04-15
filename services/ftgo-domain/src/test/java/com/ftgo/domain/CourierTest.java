package com.ftgo.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.ftgo.common.Address;
import com.ftgo.common.PersonName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Courier} entity.
 *
 * <p>Demonstrates testing of:
 *
 * <ul>
 *   <li>Courier construction
 *   <li>Availability state changes
 * </ul>
 */
@DisplayName("Courier")
class CourierTest {

    private Courier courier;

    @BeforeEach
    void setUp() {
        courier =
                new Courier(
                        new PersonName("Alex", "Driver"),
                        new Address("100 Delivery Ln", null, "Oakland", "CA", "94612"));
    }

    @Nested
    @DisplayName("availability")
    class Availability {

        @Test
        @DisplayName("should be available after noteAvailable()")
        void noteAvailable_shouldSetAvailableTrue() {
            courier.noteAvailable();
            assertThat(courier.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("should be unavailable after noteUnavailable()")
        void noteUnavailable_shouldSetAvailableFalse() {
            courier.noteAvailable();
            courier.noteUnavailable();
            assertThat(courier.isAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should create courier with name and address")
        void constructor_withNameAndAddress_createsCourier() {
            assertThat(courier).isNotNull();
            assertThat(courier.getId()).isNull(); // Not persisted yet
        }
    }
}
