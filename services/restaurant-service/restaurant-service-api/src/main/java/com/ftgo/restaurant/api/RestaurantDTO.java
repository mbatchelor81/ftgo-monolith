package com.ftgo.restaurant.api;

import net.chrisrichardson.ftgo.common.Address;

import java.util.List;

public class RestaurantDTO {

  private long id;
  private String name;
  private Address address;
  private List<MenuItemDTO> menuItems;

  private RestaurantDTO() {
  }

  public RestaurantDTO(long id, String name, Address address, List<MenuItemDTO> menuItems) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.menuItems = menuItems;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public List<MenuItemDTO> getMenuItems() {
    return menuItems;
  }

  public void setMenuItems(List<MenuItemDTO> menuItems) {
    this.menuItems = menuItems;
  }
}
