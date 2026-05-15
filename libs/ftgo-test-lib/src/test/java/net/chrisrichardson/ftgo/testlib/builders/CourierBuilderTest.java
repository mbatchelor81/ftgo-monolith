package net.chrisrichardson.ftgo.testlib.builders;

import net.chrisrichardson.ftgo.domain.Courier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourierBuilderTest {

    @Test
    void aCourier_withDefaults_createsAvailableCourier() {
        Courier courier = CourierBuilder.aCourier().build();

        assertThat(courier).isNotNull();
        assertThat(courier.isAvailable()).isTrue();
    }

    @Test
    void aCourier_unavailable_setsAvailableToFalse() {
        Courier courier = CourierBuilder.aCourier()
                .withAvailable(false)
                .build();

        assertThat(courier.isAvailable()).isFalse();
    }

    @Test
    void aCourier_withId_setsIdCorrectly() {
        Courier courier = CourierBuilder.aCourier()
                .withId(7L)
                .build();

        assertThat(courier.getId()).isEqualTo(7L);
    }
}
