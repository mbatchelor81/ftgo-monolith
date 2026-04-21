package com.ftgo.test.builders;

import com.ftgo.test.fixtures.RestaurantFixture;
import com.ftgo.test.fixtures.RestaurantFixture.MenuItemFixture;
import net.chrisrichardson.ftgo.common.Money;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for {@link RestaurantFixture} test fixtures.
 *
 * <p>Defaults model the "Ajanta" restaurant used across the legacy
 * monolith tests so existing assertions continue to work when they are
 * migrated to use this builder.
 *
 * <pre>{@code
 * RestaurantFixture ajanta = RestaurantBuilder.aRestaurant()
 *     .withId(1L)
 *     .withName("Ajanta")
 *     .withMenuItem("chicken-vindaloo", "Chicken Vindaloo", new Money("12.50"))
 *     .build();
 * }</pre>
 */
public final class RestaurantBuilder {

    private Long id = 1L;
    private String name = "Ajanta";
    private String city = "Oakland";
    private final List<MenuItemFixture> menu = new ArrayList<>();

    private RestaurantBuilder() {
    }

    public static RestaurantBuilder aRestaurant() {
        return new RestaurantBuilder();
    }

    public RestaurantBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public RestaurantBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public RestaurantBuilder withCity(String city) {
        this.city = city;
        return this;
    }

    public RestaurantBuilder withMenuItem(String itemId, String itemName, Money price) {
        this.menu.add(new MenuItemFixture(itemId, itemName, price));
        return this;
    }

    public RestaurantFixture build() {
        return new RestaurantFixture(id, name, city, List.copyOf(menu));
    }
}
