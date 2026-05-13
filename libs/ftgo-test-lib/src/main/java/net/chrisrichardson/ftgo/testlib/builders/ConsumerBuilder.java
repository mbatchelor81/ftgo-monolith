package net.chrisrichardson.ftgo.testlib.builders;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Consumer;

import java.lang.reflect.Field;

/**
 * Test data builder for {@link Consumer} entities.
 *
 * <p>Usage:
 * <pre>
 * Consumer consumer = ConsumerBuilder.aConsumer()
 *     .withFirstName("John")
 *     .withLastName("Doe")
 *     .build();
 * </pre>
 */
public final class ConsumerBuilder {

    private Long id;
    private String firstName = "John";
    private String lastName = "Doe";

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

    public Consumer build() {
        Consumer consumer = new Consumer(new PersonName(firstName, lastName));

        if (id != null) {
            setField(consumer, "id", id);
        }

        return consumer;
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
