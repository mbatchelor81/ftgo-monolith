package com.ftgo.test.builders;

/**
 * Test data builder for Consumer objects.
 * Usage: ConsumerBuilder.aConsumer().withName("John", "Doe").build()
 */
public class ConsumerBuilder {

    private Long id;
    private String firstName = "Test";
    private String lastName = "Consumer";

    private ConsumerBuilder() {}

    public static ConsumerBuilder aConsumer() {
        return new ConsumerBuilder();
    }

    public ConsumerBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ConsumerBuilder withName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        return this;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
