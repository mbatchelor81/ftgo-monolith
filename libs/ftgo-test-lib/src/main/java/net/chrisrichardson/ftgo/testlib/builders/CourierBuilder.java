package net.chrisrichardson.ftgo.testlib.builders;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;

import java.lang.reflect.Field;

/**
 * Test data builder for {@link Courier} entities.
 *
 * <p>Usage:
 * <pre>
 * Courier courier = CourierBuilder.aCourier()
 *     .withAvailable(true)
 *     .build();
 * </pre>
 */
public final class CourierBuilder {

    private Long id;
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

    public CourierBuilder withAvailable(boolean available) {
        this.available = available;
        return this;
    }

    public Courier build() {
        Courier courier = new Courier(new PersonName("Test", "Courier"), new Address("1 Main St", "", "Oakland", "CA", "94611"));
        if (available) {
            courier.noteAvailable();
        } else {
            courier.noteUnavailable();
        }

        if (id != null) {
            setField(courier, "id", id);
        }

        return courier;
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field '" + fieldName + "' on " + target.getClass().getSimpleName(), e);
        }
    }
}
