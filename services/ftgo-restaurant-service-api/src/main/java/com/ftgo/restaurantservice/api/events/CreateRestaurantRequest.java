package com.ftgo.restaurantservice.api.events;

import com.ftgo.common.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request DTO for creating a new restaurant. */
public class CreateRestaurantRequest {

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotNull(message = "menu must not be null")
    @Valid
    private RestaurantMenuDTO menu;

    @NotNull(message = "address must not be null")
    private Address address;

    private CreateRestaurantRequest() {}

    public CreateRestaurantRequest(String name, Address address, RestaurantMenuDTO menu) {
        this.name = name;
        this.address = address;
        this.menu = menu;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RestaurantMenuDTO getMenu() {
        return menu;
    }

    public void setMenu(RestaurantMenuDTO menu) {
        this.menu = menu;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
