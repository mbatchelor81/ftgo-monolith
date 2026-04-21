package net.chrisrichardson.ftgo.orderservice.api.web;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.List;

public class CreateOrderRequest {

  @Positive(message = "restaurantId must be positive")
  private long restaurantId;

  @Positive(message = "consumerId must be positive")
  private long consumerId;

  @NotNull(message = "lineItems is required")
  @NotEmpty(message = "lineItems must contain at least one item")
  @Size(max = 100, message = "lineItems may not exceed 100 entries")
  @Valid
  private List<LineItem> lineItems;

  public CreateOrderRequest(long consumerId, long restaurantId, List<LineItem> lineItems) {
    this.restaurantId = restaurantId;
    this.consumerId = consumerId;
    this.lineItems = lineItems;

  }

  private CreateOrderRequest() {
  }

  public long getRestaurantId() {
    return restaurantId;
  }

  public void setRestaurantId(long restaurantId) {
    this.restaurantId = restaurantId;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(long consumerId) {
    this.consumerId = consumerId;
  }

  public List<LineItem> getLineItems() {
    return lineItems;
  }

  public void setLineItems(List<LineItem> lineItems) {
    this.lineItems = lineItems;
  }

  public static class LineItem {

    @NotNull(message = "menuItemId is required")
    @Size(min = 1, max = 64, message = "menuItemId must be 1-64 characters")
    private String menuItemId;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;

    private LineItem() {
    }

    public LineItem(String menuItemId, int quantity) {
      this.menuItemId = menuItemId;

      this.quantity = quantity;
    }

    public String getMenuItemId() {
      return menuItemId;
    }

    public int getQuantity() {
      return quantity;
    }

    public void setQuantity(int quantity) {
      this.quantity = quantity;
    }

    public void setMenuItemId(String menuItemId) {
      this.menuItemId = menuItemId;

    }

  }


}
