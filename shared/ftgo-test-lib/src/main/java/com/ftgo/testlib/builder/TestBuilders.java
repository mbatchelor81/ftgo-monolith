package com.ftgo.testlib.builder;

/**
 * Convenience entry point for all test data builders.
 *
 * <pre>{@code
 * import static com.ftgo.testlib.builder.TestBuilders.*;
 *
 * Order order = order().withConsumerId(42L).build();
 * Consumer consumer = consumer().withFirstName("Jane").build();
 * Restaurant restaurant = restaurant().withName("Ajanta").build();
 * Courier courier = courier().available().build();
 * }</pre>
 */
public final class TestBuilders {

    private TestBuilders() {
    }

    public static OrderBuilder order() {
        return OrderBuilder.order();
    }

    public static ConsumerBuilder consumer() {
        return ConsumerBuilder.consumer();
    }

    public static RestaurantBuilder restaurant() {
        return RestaurantBuilder.restaurant();
    }

    public static CourierBuilder courier() {
        return CourierBuilder.courier();
    }

    public static MenuItemBuilder menuItem() {
        return MenuItemBuilder.menuItem();
    }

    public static AddressBuilder address() {
        return AddressBuilder.address();
    }

    public static MoneyBuilder money() {
        return MoneyBuilder.money();
    }
}
