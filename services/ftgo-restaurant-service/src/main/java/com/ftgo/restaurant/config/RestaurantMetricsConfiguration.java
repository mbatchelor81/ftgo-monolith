package com.ftgo.restaurant.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Configures custom business metrics for the Restaurant Service.
 *
 * <p>Metrics exposed:
 * <ul>
 *   <li>{@code ftgo.restaurants.created} — counter of restaurants created</li>
 *   <li>{@code ftgo.restaurants.menu.revised} — counter of menu revisions</li>
 *   <li>{@code ftgo.restaurants.tickets.accepted} — counter of tickets accepted</li>
 *   <li>{@code ftgo.restaurants.tickets.preparing} — counter of tickets moved to preparing</li>
 *   <li>{@code ftgo.restaurants.total} — gauge of total restaurants</li>
 *   <li>{@code ftgo.restaurants.ticket.processing.duration} — timer for ticket processing</li>
 * </ul>
 */
@Configuration
public class RestaurantMetricsConfiguration {

    private final AtomicLong totalRestaurants = new AtomicLong(0);

    @Bean
    public Counter restaurantsCreatedCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.restaurants.created")
                .description("Total number of restaurants created")
                .tag("service", "ftgo-restaurant-service")
                .register(registry);
    }

    @Bean
    public Counter restaurantsMenuRevisedCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.restaurants.menu.revised")
                .description("Total number of restaurant menu revisions")
                .tag("service", "ftgo-restaurant-service")
                .register(registry);
    }

    @Bean
    public Counter restaurantsTicketsAcceptedCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.restaurants.tickets.accepted")
                .description("Total number of restaurant tickets accepted")
                .tag("service", "ftgo-restaurant-service")
                .register(registry);
    }

    @Bean
    public Counter restaurantsTicketsPreparingCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.restaurants.tickets.preparing")
                .description("Total number of tickets moved to preparing state")
                .tag("service", "ftgo-restaurant-service")
                .register(registry);
    }

    @Bean
    public AtomicLong totalRestaurantsGauge(MeterRegistry registry) {
        Gauge.builder("ftgo.restaurants.total", totalRestaurants, AtomicLong::doubleValue)
                .description("Total number of registered restaurants")
                .tag("service", "ftgo-restaurant-service")
                .register(registry);
        return totalRestaurants;
    }

    @Bean
    public Timer restaurantTicketProcessingTimer(MeterRegistry registry) {
        return Timer.builder("ftgo.restaurants.ticket.processing.duration")
                .description("Time taken to process a restaurant ticket")
                .tag("service", "ftgo-restaurant-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }
}
