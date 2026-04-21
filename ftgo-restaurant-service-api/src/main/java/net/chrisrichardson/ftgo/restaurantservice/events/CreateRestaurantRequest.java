package net.chrisrichardson.ftgo.restaurantservice.events;


import net.chrisrichardson.ftgo.common.Address;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateRestaurantRequest {

  @NotBlank(message = "name is required")
  @Size(max = 200, message = "name must be 200 characters or fewer")
  private String name;

  @NotNull(message = "menu is required")
  @Valid
  private RestaurantMenuDTO menu;

  @NotNull(message = "address is required")
  @Valid
  private Address address;

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

  private CreateRestaurantRequest() {

  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public Address getAddress() {
    return address;
  }
}
