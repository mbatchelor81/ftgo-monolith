package net.chrisrichardson.ftgo.testlib.builders;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Test data builder for {@link Restaurant} entities.
 *
 * <p>Usage:
 * <pre>
 * Restaurant restaurant = RestaurantBuilder.aRestaurant()
 *     .withName("Ajanta")
 *     .withMenuItem("1", "Chicken Vindaloo", new Money("12.34"))
 *     .build();
 * </pre>
 */
public final class RestaurantBuilder {

    private long id = 1L;
    private String name = "Test Restaurant";
    private final List<MenuItem> menuItems = new ArrayList<>();

    private RestaurantBuilder() {
    }

    public static RestaurantBuilder aRestaurant() {
        return new RestaurantBuilder();
    }

    public RestaurantBuilder withId(long id) {
        this.id = id;
        return this;
    }

    public RestaurantBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public RestaurantBuilder withMenuItem(String itemId, String itemName, Money price) {
        menuItems.add(new MenuItem(itemId, itemName, price));
        return this;
    }

    public Restaurant build() {
        List<MenuItem> items = menuItems.isEmpty()
                ? List.of(new MenuItem("1", "Default Item", new Money("10.00")))
                : menuItems;

        return new Restaurant(id, name, new RestaurantMenu(items));
    }
}
