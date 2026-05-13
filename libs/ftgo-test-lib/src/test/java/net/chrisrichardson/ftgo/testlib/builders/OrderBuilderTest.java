package net.chrisrichardson.ftgo.testlib.builders;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.domain.Restaurant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderBuilderTest {

    @Test
    void anOrder_withDefaults_createsApprovedOrder() {
        Order order = OrderBuilder.anOrder().build();

        assertThat(order).isNotNull();
        assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
        assertThat(order.getConsumerId()).isEqualTo(1L);
        assertThat(order.getLineItems()).hasSize(1);
    }

    @Test
    void anOrder_withCustomValues_setsFieldsCorrectly() {
        Restaurant restaurant = RestaurantBuilder.aRestaurant()
                .withName("Custom Restaurant")
                .build();

        Order order = OrderBuilder.anOrder()
                .withId(42L)
                .withConsumerId(99L)
                .withRestaurant(restaurant)
                .withLineItem("1", "Pizza", new Money("15.00"), 2)
                .build();

        assertThat(order.getId()).isEqualTo(42L);
        assertThat(order.getConsumerId()).isEqualTo(99L);
        assertThat(order.getRestaurant().getName()).isEqualTo("Custom Restaurant");
        assertThat(order.getLineItems()).hasSize(1);
    }

    @Test
    void anOrder_withMultipleLineItems_createsAll() {
        Order order = OrderBuilder.anOrder()
                .withLineItem("1", "Item A", new Money("10.00"), 1)
                .withLineItem("2", "Item B", new Money("20.00"), 2)
                .build();

        assertThat(order.getLineItems()).hasSize(2);
    }
}
