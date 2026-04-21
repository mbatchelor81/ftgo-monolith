package com.ftgo.restaurant.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Business metrics published by the Restaurant microservice.
 *
 * <p>The {@code restaurants.menu.updated} counter intentionally tracks
 * every menu mutation (add / update / remove item) so it can be rolled up
 * per-restaurant in Grafana via the {@code restaurant_id} tag once the
 * endpoint lands.
 */
@Component
public class RestaurantMetrics {

    public static final String RESTAURANTS_CREATED = "restaurants.created";
    public static final String RESTAURANTS_MENU_UPDATED = "restaurants.menu.updated";
    public static final String ORDERS_ACCEPTED = "restaurants.orders.accepted";
    public static final String ORDER_PREPARATION_TIME = "restaurants.order.preparation.time";

    private final Counter restaurantsCreated;
    private final Counter menuUpdates;
    private final Counter ordersAccepted;
    private final Timer preparationTimer;

    public RestaurantMetrics(MeterRegistry registry) {
        this.restaurantsCreated = Counter.builder(RESTAURANTS_CREATED)
                .description("Total number of restaurants onboarded")
                .tag("service", "restaurant-service")
                .register(registry);

        this.menuUpdates = Counter.builder(RESTAURANTS_MENU_UPDATED)
                .description("Total number of restaurant menu mutations (add / update / remove item)")
                .tag("service", "restaurant-service")
                .register(registry);

        this.ordersAccepted = Counter.builder(ORDERS_ACCEPTED)
                .description("Total number of orders accepted by restaurants")
                .tag("service", "restaurant-service")
                .register(registry);

        this.preparationTimer = Timer.builder(ORDER_PREPARATION_TIME)
                .description("Time taken from restaurant order acceptance to ready-for-pickup")
                .tag("service", "restaurant-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);
    }

    public void recordRestaurantCreated() {
        restaurantsCreated.increment();
    }

    public void recordMenuUpdated() {
        menuUpdates.increment();
    }

    public void recordOrderAccepted() {
        ordersAccepted.increment();
    }

    public Timer preparationTimer() {
        return preparationTimer;
    }
}
