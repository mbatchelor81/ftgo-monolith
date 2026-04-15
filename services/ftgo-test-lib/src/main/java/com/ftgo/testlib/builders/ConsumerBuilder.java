package com.ftgo.testlib.builders;

import com.ftgo.common.PersonName;
import com.ftgo.domain.Consumer;

/**
 * Fluent builder for creating {@link Consumer} instances in tests.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * var consumer = ConsumerBuilder.aConsumer()
 *     .withFirstName("Jane")
 *     .withLastName("Doe")
 *     .build();
 * }</pre>
 */
public final class ConsumerBuilder {

    private String firstName = "John";
    private String lastName = "Doe";

    private ConsumerBuilder() {}

    /** Creates a new builder with sensible defaults. */
    public static ConsumerBuilder aConsumer() {
        return new ConsumerBuilder();
    }

    /** Sets the first name. */
    public ConsumerBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /** Sets the last name. */
    public ConsumerBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /** Builds the {@link Consumer} instance. */
    public Consumer build() {
        return new Consumer(new PersonName(firstName, lastName));
    }
}
