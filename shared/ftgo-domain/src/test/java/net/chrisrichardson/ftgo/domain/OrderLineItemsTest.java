package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OrderLineItemsTest {

  @Test
  void orderTotal_sumsAllLineItems() {
    List<OrderLineItem> items = List.of(
        new OrderLineItem("1", "Pizza", new Money("10.00"), 2),
        new OrderLineItem("2", "Salad", new Money("5.00"), 1)
    );
    OrderLineItems orderLineItems = new OrderLineItems(items);

    assertThat(orderLineItems.orderTotal()).isEqualTo(new Money("25.00"));
  }

  @Test
  void lineItemQuantityChange_computesDelta() {
    List<OrderLineItem> items = List.of(
        new OrderLineItem("1", "Pizza", new Money("10.00"), 2),
        new OrderLineItem("2", "Salad", new Money("5.00"), 1)
    );
    OrderLineItems orderLineItems = new OrderLineItems(items);

    OrderRevision revision = new OrderRevision(
        Optional.empty(),
        Map.of("1", 3)
    );

    LineItemQuantityChange change = orderLineItems.lineItemQuantityChange(revision);

    assertThat(change.getCurrentOrderTotal()).isEqualTo(new Money("25.00"));
    assertThat(change.getDelta()).isEqualTo(new Money("10.00"));
    assertThat(change.getNewOrderTotal()).isEqualTo(new Money("35.00"));
  }
}
