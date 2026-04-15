package com.ftgo.testlib.builders;

import com.ftgo.common.Address;
import com.ftgo.common.Money;
import com.ftgo.domain.MenuItem;
import com.ftgo.domain.Restaurant;
import com.ftgo.domain.RestaurantMenu;
import java.util.List;

/**
 * Fluent builder for creating {@link Restaurant} instances in tests.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * var restaurant = RestaurantBuilder.aRestaurant()
 *     .withName("Test Kitchen")
 *     .build();
 * }</pre>
 */
public final class RestaurantBuilder {

    private String name = "Ajanta";
    private Address address = new Address("1 Main St", "Suite 100", "Oakland", "CA", "94609");
    private List<MenuItem> menuItems =
            List.of(
                    new MenuItem("menu-1", "Chicken Vindaloo", new Money("12.34")),
                    new MenuItem("menu-2", "Lamb Biryani", new Money("15.99")));

    private RestaurantBuilder() {}

    /** Creates a new builder with sensible defaults. */
    public static RestaurantBuilder aRestaurant() {
        return new RestaurantBuilder();
    }

    /** Sets the restaurant name. */
    public RestaurantBuilder withName(String name) {
        this.name = name;
        return this;
    }

    /** Sets the restaurant address. */
    public RestaurantBuilder withAddress(Address address) {
        this.address = address;
        return this;
    }

    /** Sets the menu items. */
    public RestaurantBuilder withMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
        return this;
    }

    /** Builds the {@link Restaurant} instance. */
    public Restaurant build() {
        return new Restaurant(name, address, new RestaurantMenu(menuItems));
    }
}
