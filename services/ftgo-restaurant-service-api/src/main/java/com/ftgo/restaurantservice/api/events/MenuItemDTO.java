package com.ftgo.restaurantservice.api.events;

import com.ftgo.common.Money;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** DTO representing a menu item. */
public class MenuItemDTO {

    @NotBlank(message = "id must not be blank")
    private String id;

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotNull(message = "price must not be null")
    private Money price;

    private MenuItemDTO() {}

    public MenuItemDTO(String id, String name, Money price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Money getPrice() {
        return price;
    }

    public void setPrice(Money price) {
        this.price = price;
    }
}
