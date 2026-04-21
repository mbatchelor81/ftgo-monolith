package com.ftgo.test.builders;

import com.ftgo.test.fixtures.ConsumerFixture;

/**
 * Fluent builder for {@link ConsumerFixture} test fixtures.
 *
 * <p>Produces a lightweight, framework-agnostic DTO that service tests can
 * use directly or map into their own JPA entities. The builder is
 * deliberately forgiving — every field has a sensible default, so tests
 * only have to set the attributes that are relevant to the assertion
 * being made.
 *
 * <pre>{@code
 * ConsumerFixture consumer = ConsumerBuilder.aConsumer()
 *     .withId(42L)
 *     .withFirstName("Ada")
 *     .withLastName("Lovelace")
 *     .build();
 * }</pre>
 */
public final class ConsumerBuilder {

    private Long id = 1L;
    private String firstName = "Jane";
    private String lastName = "Doe";
    private String email = "jane.doe@example.com";

    private ConsumerBuilder() {
    }

    public static ConsumerBuilder aConsumer() {
        return new ConsumerBuilder();
    }

    public ConsumerBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ConsumerBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public ConsumerBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public ConsumerBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public ConsumerFixture build() {
        return new ConsumerFixture(id, firstName, lastName, email);
    }
}
