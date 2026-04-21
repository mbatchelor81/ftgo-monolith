package com.ftgo.courier.examples;

import static org.assertj.core.api.Assertions.assertThat;

import com.ftgo.test.builders.CourierBuilder;
import com.ftgo.test.fixtures.CourierFixture;
import com.ftgo.test.fixtures.FtgoMothers;
import org.junit.jupiter.api.Test;

/**
 * Unit-tier example for the Courier bounded context.
 *
 * <p>Courier fixtures carry availability state that gates the scheduling logic in the legacy
 * monolith's {@code CourierRepository.findAllAvailable()} query. The builder exposes {@link
 * CourierBuilder#available()} / {@link CourierBuilder#unavailable()} to make that flip explicit in
 * test narratives.
 */
class CourierFixtureExampleTest {

  @Test
  void aCourier_withDefaults_isAvailable() {
    CourierFixture courier = CourierBuilder.aCourier().build();

    assertThat(courier.available()).isTrue();
  }

  @Test
  void aCourier_markedUnavailable_flipsFlag() {
    CourierFixture courier = CourierBuilder.aCourier().unavailable().build();

    assertThat(courier.available()).isFalse();
  }

  @Test
  void availableCourier_mirrorsLegacyMonolithMother() {
    CourierFixture courier = FtgoMothers.availableCourier().build();

    assertThat(courier.available()).isTrue();
    assertThat(courier.firstName()).isNotBlank();
    assertThat(courier.lastName()).isNotBlank();
  }
}
