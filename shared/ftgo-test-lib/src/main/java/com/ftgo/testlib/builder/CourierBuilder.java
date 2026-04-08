package com.ftgo.testlib.builder;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;

/**
 * Builder for {@link Courier} test instances.
 *
 * <pre>{@code
 * Courier courier = CourierBuilder.courier()
 *     .withFirstName("Alex")
 *     .available()
 *     .build();
 * }</pre>
 */
public final class CourierBuilder {

    private String firstName = "Alex";
    private String lastName = "Smith";
    private Address address = AddressBuilder.address().build();
    private boolean available = true;

    private CourierBuilder() {
    }

    public static CourierBuilder courier() {
        return new CourierBuilder();
    }

    public CourierBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public CourierBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public CourierBuilder withAddress(Address address) {
        this.address = address;
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
