package com.ftgo.testlib.builder;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link Restaurant} test instances.
 *
 * <pre>{@code
 * Restaurant restaurant = RestaurantBuilder.restaurant()
 *     .withName("Ajanta")
 *     .withMenuItem("1", "Chicken Vindaloo", "12.99")
 *     .build();
 * }</pre>
 */
public final class RestaurantBuilder {

    private Long id;
    private String name = "Ajanta";
    private Address address = AddressBuilder.address().build();
    private final List<MenuItem> menuItems = new ArrayList<>();

    private RestaurantBuilder() {
        menuItems.add(MenuItemBuilder.menuItem().build());
    }

    public static RestaurantBuilder restaurant() {
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

    public RestaurantBuilder withAddress(Address address) {
        this.address = address;
        return this;
    }

    public RestaurantBuilder withMenuItem(String id, String name, String price) {
        this.menuItems.add(new MenuItem(id, name, new Money(price)));
        return this;
    }

    public RestaurantBuilder withMenuItems(List<MenuItem> menuItems) {
        this.menuItems.clear();
        this.menuItems.addAll(menuItems);
        return this;
    }

    public RestaurantBuilder clearMenuItems() {
        this.menuItems.clear();
        return this;
    }

    public Restaurant build() {
        RestaurantMenu menu = new RestaurantMenu(menuItems);
        if (id != null) {
            return new Restaurant(id, name, menu);
        }
        return new Restaurant(name, address, menu);
    }
}
