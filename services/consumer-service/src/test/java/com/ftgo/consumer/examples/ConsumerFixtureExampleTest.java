package com.ftgo.consumer.examples;

import static org.assertj.core.api.Assertions.assertThat;

import com.ftgo.test.builders.ConsumerBuilder;
import com.ftgo.test.fixtures.ConsumerFixture;
import com.ftgo.test.fixtures.FtgoMothers;
import org.junit.jupiter.api.Test;

/**
 * Unit-tier example for the Consumer bounded context.
 *
 * <p>Demonstrates the EM-48 testing toolkit on the consumer side:
 *
 * <ul>
 *   <li>{@link ConsumerBuilder} for fluent, minimal test-data setup.
 *   <li>{@link FtgoMothers} object-mother constants that mirror the canonical fixtures used by the
 *       legacy monolith's Consumer tests so assertions can be kept stable during the migration.
 * </ul>
 *
 * <p>No Spring context is booted and no collaborators are needed; this is the cheapest tier on the
 * pyramid (see {@code docs/testing-strategy.md} §2 and {@code
 * docs/testing/when-to-write-which-test.md} §3).
 *
 * <p>Copy this class when writing the first real Consumer domain test: replace the fixture
 * assertions with assertions on the system under test (e.g. {@code ConsumerService}, {@code
 * CreateConsumerCommandHandler}).
 */
class ConsumerFixtureExampleTest {

  @Test
  void aConsumer_withDefaults_producesJaneDoe() {
    ConsumerFixture consumer = ConsumerBuilder.aConsumer().build();

    assertThat(consumer.firstName()).isEqualTo("Jane");
    assertThat(consumer.lastName()).isEqualTo("Doe");
    assertThat(consumer.email()).isEqualTo("jane.doe@example.com");
  }

  @Test
  void defaultConsumer_matchesLegacyMonolithFixture() {
    ConsumerFixture consumer = FtgoMothers.defaultConsumer().build();

    assertThat(consumer.id()).isEqualTo(FtgoMothers.CONSUMER_ID);
  }

  @Test
  void aConsumer_overridesCustomerAttributes() {
    ConsumerFixture consumer =
        ConsumerBuilder.aConsumer()
            .withFirstName("Chris")
            .withLastName("Richardson")
            .withEmail("chris@example.com")
            .build();

    assertThat(consumer.firstName()).isEqualTo("Chris");
    assertThat(consumer.lastName()).isEqualTo("Richardson");
    assertThat(consumer.email()).isEqualTo("chris@example.com");
  }
}
