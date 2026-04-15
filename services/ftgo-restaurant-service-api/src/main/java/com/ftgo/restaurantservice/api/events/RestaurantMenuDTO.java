package com.ftgo.restaurantservice.api.events;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** DTO representing a restaurant menu. */
public class RestaurantMenuDTO {

    @NotEmpty(message = "menuItems must not be empty")
    @Valid
    private List<MenuItemDTO> menuItems;

    private RestaurantMenuDTO() {}

    public RestaurantMenuDTO(List<MenuItemDTO> menuItems) {
        this.menuItems = menuItems;
    }

    public List<MenuItemDTO> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItemDTO> menuItems) {
        this.menuItems = menuItems;
    }
}
