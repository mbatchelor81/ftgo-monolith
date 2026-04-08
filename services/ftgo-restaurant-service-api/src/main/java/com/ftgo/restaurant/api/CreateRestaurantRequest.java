package com.ftgo.restaurant.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for registering a new restaurant.
 */
public class CreateRestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    @NotNull(message = "Address is required")
    @Valid
    private AddressDto address;

    @NotNull(message = "Menu items are required")
    @Size(min = 1, message = "At least one menu item is required")
    private List<@Valid MenuItemDto> menuItems;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AddressDto getAddress() {
        return address;
    }

    public void setAddress(AddressDto address) {
        this.address = address;
    }

    public List<MenuItemDto> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItemDto> menuItems) {
        this.menuItems = menuItems;
    }

    public static class AddressDto {
        @NotBlank(message = "Street is required")
        private String street1;

        private String street2;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "State is required")
        private String state;

        @NotBlank(message = "Zip code is required")
        private String zip;

        public String getStreet1() { return street1; }
        public void setStreet1(String street1) { this.street1 = street1; }
        public String getStreet2() { return street2; }
        public void setStreet2(String street2) { this.street2 = street2; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getZip() { return zip; }
        public void setZip(String zip) { this.zip = zip; }
    }

    public static class MenuItemDto {
        @NotBlank(message = "Menu item ID is required")
        private String id;

        @NotBlank(message = "Menu item name is required")
        private String name;

        @NotNull(message = "Price is required")
        private String price;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
    }
}
