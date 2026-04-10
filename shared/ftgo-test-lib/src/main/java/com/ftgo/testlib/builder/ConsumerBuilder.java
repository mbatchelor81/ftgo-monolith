package com.ftgo.testlib.builder;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Consumer;

/**
 * Builder for {@link Consumer} test instances.
 *
 * <pre>{@code
 * Consumer consumer = ConsumerBuilder.consumer()
 *     .withFirstName("Jane")
 *     .withLastName("Doe")
 *     .build();
 * }</pre>
 */
public final class ConsumerBuilder {

    private String firstName = "John";
    private String lastName = "Doe";

    private ConsumerBuilder() {
    }

    public static ConsumerBuilder consumer() {
        return new ConsumerBuilder();
    }

    public ConsumerBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public ConsumerBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public ConsumerBuilder withName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        return this;
    }

    public Consumer build() {
        return new Consumer(new PersonName(firstName, lastName));
    }
}
