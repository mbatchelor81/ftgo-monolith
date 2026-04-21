package com.ftgo.test.fixtures;

import net.chrisrichardson.ftgo.common.Money;

import java.util.List;

/**
 * Immutable test fixture representing a restaurant for use in service tests.
 *
 * @param id       restaurant identity
 * @param name     display name
 * @param city     billing city, used by authorization rules in some tests
 * @param menu     snapshot of menu items
 */
public record RestaurantFixture(Long id, String name, String city, List<MenuItemFixture> menu) {

    /**
     * @param id    menu item identity (stable within a restaurant)
     * @param name  display name
     * @param price item unit price
     */
    public record MenuItemFixture(String id, String name, Money price) {
    }
}
