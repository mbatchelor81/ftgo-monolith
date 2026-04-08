package com.ftgo.testlib.builder;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.MenuItem;

/**
 * Builder for {@link MenuItem} test instances.
 *
 * <pre>{@code
 * MenuItem item = MenuItemBuilder.menuItem()
 *     .withName("Pad Thai")
 *     .withPrice("12.99")
 *     .build();
 * }</pre>
 */
public final class MenuItemBuilder {

    private String id = "menu-item-1";
    private String name = "Chicken Vindaloo";
    private Money price = new Money("12.99");

    private MenuItemBuilder() {
    }

    public static MenuItemBuilder menuItem() {
        return new MenuItemBuilder();
    }

    public MenuItemBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public MenuItemBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public MenuItemBuilder withPrice(String price) {
        this.price = new Money(price);
        return this;
    }

    public MenuItemBuilder withPrice(Money price) {
        this.price = price;
        return this;
    }

    public MenuItem build() {
        return new MenuItem(id, name, price);
    }
}
