package com.ftgo.testlib.builders;

import com.ftgo.common.Address;
import com.ftgo.common.PersonName;
import com.ftgo.domain.Courier;

/**
 * Fluent builder for creating {@link Courier} instances in tests.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * var courier = CourierBuilder.aCourier()
 *     .withFirstName("Jane")
 *     .available()
 *     .build();
 * }</pre>
 */
public final class CourierBuilder {

    private String firstName = "Alex";
    private String lastName = "Courier";
    private Address address = new Address("100 Delivery Ln", null, "Oakland", "CA", "94612");
    private boolean available = true;

    private CourierBuilder() {}

    /** Creates a new builder with sensible defaults. */
    public static CourierBuilder aCourier() {
        return new CourierBuilder();
    }

    /** Sets the first name. */
    public CourierBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /** Sets the last name. */
    public CourierBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /** Sets the address. */
    public CourierBuilder withAddress(Address address) {
        this.address = address;
        return this;
    }

    /** Sets the courier as available. */
    public CourierBuilder available() {
        this.available = true;
        return this;
    }

    /** Sets the courier as unavailable. */
    public CourierBuilder unavailable() {
        this.available = false;
        return this;
    }

    /** Builds the {@link Courier} instance. */
    public Courier build() {
        Courier courier = new Courier(new PersonName(firstName, lastName), address);
        if (available) {
            courier.noteAvailable();
        } else {
            courier.noteUnavailable();
        }
        return courier;
    }
}
