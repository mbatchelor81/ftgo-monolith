package com.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics for the Restaurant Service.
 *
 * <p>Tracks restaurant creation and menu revision events.
 */
@Component
@ConditionalOnProperty(name = "spring.application.name", havingValue = "ftgo-restaurant-service")
public class RestaurantMetrics {

    private final Counter restaurantsCreated;
    private final Counter menuRevisionsPerformed;

    public RestaurantMetrics(MeterRegistry registry) {
        this.restaurantsCreated = Counter.builder("ftgo.restaurants.created")
                .description("Total number of restaurants created")
                .tag("service", "restaurant-service")
                .register(registry);

        this.menuRevisionsPerformed = Counter.builder("ftgo.restaurants.menu.revisions")
                .description("Total number of menu revisions performed")
                .tag("service", "restaurant-service")
                .register(registry);
    }

    public void recordRestaurantCreated() {
        restaurantsCreated.increment();
    }

    public void recordMenuRevision() {
        menuRevisionsPerformed.increment();
    }
}
