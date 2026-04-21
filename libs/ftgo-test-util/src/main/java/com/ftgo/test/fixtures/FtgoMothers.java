package com.ftgo.test.fixtures;

import com.ftgo.test.builders.ConsumerBuilder;
import com.ftgo.test.builders.CourierBuilder;
import com.ftgo.test.builders.OrderBuilder;
import com.ftgo.test.builders.RestaurantBuilder;
import net.chrisrichardson.ftgo.common.Money;

/**
 * Object Mothers for the canonical FTGO test fixtures — "Ajanta"
 * restaurant, "Chicken Vindaloo" menu item, etc.
 *
 * <p>These mirror the long-standing {@code OrderDetailsMother} /
 * {@code RestaurantMother} constants in the legacy monolith so that
 * existing assertions continue to work after the migration to the new
 * test builders. New tests should prefer the fluent builders
 * (see {@link ConsumerBuilder}, {@link OrderBuilder}, etc.) and only
 * fall back to the mothers for convenience in one-liners.
 */
public final class FtgoMothers {

    public static final Long AJANTA_ID = 1L;
    public static final String AJANTA_NAME = "Ajanta";
    public static final String CHICKEN_VINDALOO_ID = "chicken-vindaloo";
    public static final String CHICKEN_VINDALOO_NAME = "Chicken Vindaloo";
    public static final Money CHICKEN_VINDALOO_PRICE = new Money("12.50");
    public static final int CHICKEN_VINDALOO_QUANTITY = 5;

    public static final Long CONSUMER_ID = 1511300065921L;
    public static final Long ORDER_ID = 99L;

    private FtgoMothers() {
    }

    public static ConsumerBuilder defaultConsumer() {
        return ConsumerBuilder.aConsumer()
                .withId(CONSUMER_ID)
                .withFirstName("Jane")
                .withLastName("Doe");
    }

    public static RestaurantBuilder ajantaRestaurant() {
        return RestaurantBuilder.aRestaurant()
                .withId(AJANTA_ID)
                .withName(AJANTA_NAME)
                .withMenuItem(CHICKEN_VINDALOO_ID, CHICKEN_VINDALOO_NAME, CHICKEN_VINDALOO_PRICE);
    }

    public static OrderBuilder chickenVindalooOrder() {
        return OrderBuilder.anOrder()
                .withId(ORDER_ID)
                .withConsumer(CONSUMER_ID)
                .withRestaurant(AJANTA_ID)
                .withState("APPROVED")
                .withLineItem(CHICKEN_VINDALOO_ID, CHICKEN_VINDALOO_NAME,
                        CHICKEN_VINDALOO_PRICE, CHICKEN_VINDALOO_QUANTITY);
    }

    public static CourierBuilder availableCourier() {
        return CourierBuilder.aCourier().withId(1L).available();
    }
}
