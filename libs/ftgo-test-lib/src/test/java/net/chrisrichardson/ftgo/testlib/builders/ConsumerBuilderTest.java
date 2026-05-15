package net.chrisrichardson.ftgo.testlib.builders;

import net.chrisrichardson.ftgo.domain.Consumer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumerBuilderTest {

    @Test
    void aConsumer_withDefaults_createsConsumer() {
        Consumer consumer = ConsumerBuilder.aConsumer().build();

        assertThat(consumer).isNotNull();
        assertThat(consumer.getName().getFirstName()).isEqualTo("John");
        assertThat(consumer.getName().getLastName()).isEqualTo("Doe");
    }

    @Test
    void aConsumer_withCustomName_setsFieldsCorrectly() {
        Consumer consumer = ConsumerBuilder.aConsumer()
                .withId(42L)
                .withFirstName("Jane")
                .withLastName("Smith")
                .build();

        assertThat(consumer.getId()).isEqualTo(42L);
        assertThat(consumer.getName().getFirstName()).isEqualTo("Jane");
        assertThat(consumer.getName().getLastName()).isEqualTo("Smith");
    }
}
