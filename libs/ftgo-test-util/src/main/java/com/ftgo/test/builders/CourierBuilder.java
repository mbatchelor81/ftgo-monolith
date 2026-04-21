package com.ftgo.test.builders;

import com.ftgo.test.fixtures.CourierFixture;

/**
 * Fluent builder for {@link CourierFixture} test fixtures.
 *
 * <pre>{@code
 * CourierFixture onShift = CourierBuilder.aCourier().available().build();
 * CourierFixture offShift = CourierBuilder.aCourier().withId(7L).unavailable().build();
 * }</pre>
 */
public final class CourierBuilder {

    private Long id = 1L;
    private String firstName = "Alex";
    private String lastName = "Rider";
    private boolean available = true;

    private CourierBuilder() {
    }

    public static CourierBuilder aCourier() {
        return new CourierBuilder();
    }

    public CourierBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public CourierBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public CourierBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public CourierBuilder available() {
        this.available = true;
        return this;
    }

    public CourierBuilder unavailable() {
        this.available = false;
        return this;
    }

    public CourierFixture build() {
        return new CourierFixture(id, firstName, lastName, available);
    }
}
