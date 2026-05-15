package com.ftgo.order.api;

import net.chrisrichardson.ftgo.common.Money;

import java.util.List;

public class OrderDTO {

  private long id;
  private String orderState;
  private long consumerId;
  private long restaurantId;
  private List<OrderLineItemDTO> lineItems;
  private Money orderTotal;

  private OrderDTO() {
  }

  public OrderDTO(long id, String orderState, long consumerId, long restaurantId,
                  List<OrderLineItemDTO> lineItems, Money orderTotal) {
    this.id = id;
    this.orderState = orderState;
    this.consumerId = consumerId;
    this.restaurantId = restaurantId;
    this.lineItems = lineItems;
    this.orderTotal = orderTotal;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getOrderState() {
    return orderState;
  }

  public void setOrderState(String orderState) {
    this.orderState = orderState;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(long consumerId) {
    this.consumerId = consumerId;
  }

  public long getRestaurantId() {
    return restaurantId;
  }

  public void setRestaurantId(long restaurantId) {
    this.restaurantId = restaurantId;
  }

  public List<OrderLineItemDTO> getLineItems() {
    return lineItems;
  }

  public void setLineItems(List<OrderLineItemDTO> lineItems) {
    this.lineItems = lineItems;
  }

  public Money getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(Money orderTotal) {
    this.orderTotal = orderTotal;
  }
}
