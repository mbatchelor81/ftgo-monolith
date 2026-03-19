package com.ftgo.test.builders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Test data builder for Restaurant-related test data.
 * Usage: RestaurantBuilder.aRestaurant().withName("Test Restaurant").build()
 */
public class RestaurantBuilder {

    private Long id;
    private String name = "Test Restaurant";
    private String street = "123 Test St";
    private String city = "Test City";
    private String state = "TS";
    private String zip = "12345";
    private List<MenuItemData> menuItems = new ArrayList<>();

    private RestaurantBuilder() {
        menuItems.add(new MenuItemData("item-1", "Test Dish", new BigDecimal("10.00")));
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

    public RestaurantBuilder withAddress(String street, String city, String state, String zip) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
        return this;
    }

    public RestaurantBuilder withMenuItem(String id, String name, BigDecimal price) {
        this.menuItems.add(new MenuItemData(id, name, price));
        return this;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZip() { return zip; }
    public List<MenuItemData> getMenuItems() { return List.copyOf(menuItems); }

    public record MenuItemData(String id, String name, BigDecimal price) {}
}
