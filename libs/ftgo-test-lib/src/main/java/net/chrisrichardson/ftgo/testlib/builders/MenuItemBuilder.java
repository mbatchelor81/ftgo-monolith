package net.chrisrichardson.ftgo.testlib.builders;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.MenuItem;

/**
 * Test data builder for {@link MenuItem} value objects.
 *
 * <p>Usage:
 * <pre>
 * MenuItem item = MenuItemBuilder.aMenuItem()
 *     .withName("Chicken Vindaloo")
 *     .withPrice(new Money("12.34"))
 *     .build();
 * </pre>
 */
public final class MenuItemBuilder {

    private String id = "1";
    private String name = "Test Menu Item";
    private Money price = new Money("10.00");

    private MenuItemBuilder() {
    }

    public static MenuItemBuilder aMenuItem() {
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

    public MenuItemBuilder withPrice(Money price) {
        this.price = price;
        return this;
    }

    public MenuItem build() {
        return new MenuItem(id, name, price);
    }
}
