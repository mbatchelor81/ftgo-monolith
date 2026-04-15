package com.ftgo.restaurantservice.api.events;

import java.util.List;

public class RestaurantMenuDTO {

    private List<MenuItemDTO> menuItems;

    private RestaurantMenuDTO() {
    }

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
