package net.chrisrichardson.ftgo.orderservice.domain;

public class MenuItemIdAndQuantity {
  private String menuItemId;
  private int quantity;

  public MenuItemIdAndQuantity() {
  }

  public MenuItemIdAndQuantity(String menuItemId, int quantity) {
    this.menuItemId = menuItemId;
    this.quantity = quantity;
  }

  public String getMenuItemId() {
    return menuItemId;
  }

  public void setMenuItemId(String menuItemId) {
    this.menuItemId = menuItemId;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
}
