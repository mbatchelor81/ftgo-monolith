package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

  private Restaurant restaurant;
  private Order order;

  @BeforeEach
  void setUp() {
    RestaurantMenu menu = new RestaurantMenu(List.of(
        new MenuItem("item-1", "Pizza", new Money("10.00"))
    ));
    restaurant = new Restaurant("Test Restaurant", null, menu);

    List<OrderLineItem> lineItems = List.of(
        new OrderLineItem("item-1", "Pizza", new Money("10.00"), 2)
    );
    order = new Order(1L, restaurant, lineItems);
  }

  @Test
  void newOrder_hasApprovedState() {
    assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
  }

  @Test
  void newOrder_computesOrderTotal() {
    assertThat(order.getOrderTotal()).isEqualTo(new Money("20.00"));
  }

  @Test
  void cancel_fromApproved_transitionsToCancelled() {
    order.cancel();
    assertThat(order.getOrderState()).isEqualTo(OrderState.CANCELLED);
  }

  @Test
  void cancel_fromNonApproved_throwsException() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    assertThatThrownBy(() -> order.cancel())
        .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  void acceptTicket_fromApproved_transitionsToAccepted() {
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    order.acceptTicket(readyBy);
    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
  }

  @Test
  void notePreparing_fromAccepted_transitionsToPreparing() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    assertThat(order.getOrderState()).isEqualTo(OrderState.PREPARING);
  }

  @Test
  void noteReadyForPickup_fromPreparing_transitionsToReadyForPickup() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    assertThat(order.getOrderState()).isEqualTo(OrderState.READY_FOR_PICKUP);
  }

  @Test
  void notePickedUp_fromReadyForPickup_transitionsToPickedUp() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    assertThat(order.getOrderState()).isEqualTo(OrderState.PICKED_UP);
  }

  @Test
  void noteDelivered_fromPickedUp_transitionsToDelivered() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    order.noteDelivered();
    assertThat(order.getOrderState()).isEqualTo(OrderState.DELIVERED);
  }

  @Test
  void fullOrderLifecycle_allTransitionsSucceed() {
    assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);

    order.acceptTicket(LocalDateTime.now().plusHours(1));
    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);

    order.notePreparing();
    assertThat(order.getOrderState()).isEqualTo(OrderState.PREPARING);

    order.noteReadyForPickup();
    assertThat(order.getOrderState()).isEqualTo(OrderState.READY_FOR_PICKUP);

    order.notePickedUp();
    assertThat(order.getOrderState()).isEqualTo(OrderState.PICKED_UP);

    order.noteDelivered();
    assertThat(order.getOrderState()).isEqualTo(OrderState.DELIVERED);
  }

  @Test
  void notePreparing_fromApproved_throwsException() {
    assertThatThrownBy(() -> order.notePreparing())
        .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  void getLineItems_returnsOrderLineItems() {
    List<OrderLineItem> items = order.getLineItems();
    assertThat(items).hasSize(1);
    assertThat(items.get(0).getMenuItemId()).isEqualTo("item-1");
    assertThat(items.get(0).getQuantity()).isEqualTo(2);
  }

  @Test
  void getConsumerId_returnsConsumerId() {
    assertThat(order.getConsumerId()).isEqualTo(1L);
  }

  @Test
  void getRestaurant_returnsRestaurant() {
    assertThat(order.getRestaurant()).isEqualTo(restaurant);
  }
}
