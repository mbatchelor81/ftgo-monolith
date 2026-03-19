package com.ftgo.test.builders;

/**
 * Test data builder for Courier-related test data.
 * Usage: CourierBuilder.aCourier().available().build()
 */
public class CourierBuilder {

    private Long id;
    private boolean available = true;

    private CourierBuilder() {}

    public static CourierBuilder aCourier() {
        return new CourierBuilder();
    }

    public CourierBuilder withId(Long id) {
        this.id = id;
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

    public Long getId() { return id; }
    public boolean isAvailable() { return available; }
}
