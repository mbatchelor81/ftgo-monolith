package com.ftgo.test.builders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BuilderTest {

    @Test
    void orderBuilder_createsOrderWithDefaults() {
        var order = OrderBuilder.anOrder();

        assertThat(order.getConsumerId()).isEqualTo(1L);
        assertThat(order.getRestaurantId()).isEqualTo(1L);
        assertThat(order.getLineItems()).hasSize(1);
        assertThat(order.getLineItems().get(0).name()).isEqualTo("Test Item");
    }

    @Test
    void orderBuilder_customValues() {
        var order = OrderBuilder.anOrder()
                .withId(42L)
                .withConsumerId(10L)
                .withRestaurantId(20L)
                .withLineItem("item-2", "Custom Item", new BigDecimal("25.50"), 3);

        assertThat(order.getId()).isEqualTo(42L);
        assertThat(order.getConsumerId()).isEqualTo(10L);
        assertThat(order.getRestaurantId()).isEqualTo(20L);
        assertThat(order.getLineItems()).hasSize(2);
    }

    @Test
    void consumerBuilder_createsConsumerWithDefaults() {
        var consumer = ConsumerBuilder.aConsumer();

        assertThat(consumer.getFirstName()).isEqualTo("Test");
        assertThat(consumer.getLastName()).isEqualTo("Consumer");
    }

    @Test
    void consumerBuilder_customValues() {
        var consumer = ConsumerBuilder.aConsumer()
                .withId(5L)
                .withName("John", "Doe");

        assertThat(consumer.getId()).isEqualTo(5L);
        assertThat(consumer.getFirstName()).isEqualTo("John");
        assertThat(consumer.getLastName()).isEqualTo("Doe");
    }

    @Test
    void restaurantBuilder_createsRestaurantWithDefaults() {
        var restaurant = RestaurantBuilder.aRestaurant();

        assertThat(restaurant.getName()).isEqualTo("Test Restaurant");
        assertThat(restaurant.getMenuItems()).hasSize(1);
    }

    @Test
    void restaurantBuilder_customValues() {
        var restaurant = RestaurantBuilder.aRestaurant()
                .withId(7L)
                .withName("Pizza Place")
                .withAddress("456 Main St", "Foodville", "CA", "90210")
                .withMenuItem("pizza-1", "Margherita", new BigDecimal("12.99"));

        assertThat(restaurant.getId()).isEqualTo(7L);
        assertThat(restaurant.getName()).isEqualTo("Pizza Place");
        assertThat(restaurant.getCity()).isEqualTo("Foodville");
        assertThat(restaurant.getMenuItems()).hasSize(2);
    }

    @Test
    void courierBuilder_createsAvailableCourierByDefault() {
        var courier = CourierBuilder.aCourier();

        assertThat(courier.isAvailable()).isTrue();
    }

    @Test
    void courierBuilder_unavailable() {
        var courier = CourierBuilder.aCourier().unavailable();

        assertThat(courier.isAvailable()).isFalse();
    }
}
