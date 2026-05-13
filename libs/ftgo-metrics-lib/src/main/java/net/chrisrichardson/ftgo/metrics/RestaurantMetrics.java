package net.chrisrichardson.ftgo.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Business metrics for the Restaurant Service.
 * Tracks restaurant onboarding, ticket lifecycle, and menu operations.
 */
public class RestaurantMetrics {

    private final Counter restaurantsCreated;
    private final Counter ticketsCreated;
    private final Counter ticketsAccepted;
    private final Counter ticketsPreparing;
    private final Counter ticketsReadyForPickup;
    private final Counter menuRevisions;
    private final Timer ticketPreparationTime;

    public RestaurantMetrics(MeterRegistry registry) {
        restaurantsCreated = Counter.builder("ftgo.restaurants.created")
                .description("Total number of restaurants onboarded")
                .register(registry);

        ticketsCreated = Counter.builder("ftgo.restaurants.tickets.created")
                .description("Total number of kitchen tickets created")
                .register(registry);

        ticketsAccepted = Counter.builder("ftgo.restaurants.tickets.accepted")
                .description("Total number of tickets accepted by restaurants")
                .register(registry);

        ticketsPreparing = Counter.builder("ftgo.restaurants.tickets.preparing")
                .description("Total number of tickets in preparation")
                .register(registry);

        ticketsReadyForPickup = Counter.builder("ftgo.restaurants.tickets.ready")
                .description("Total number of tickets ready for courier pickup")
                .register(registry);

        menuRevisions = Counter.builder("ftgo.restaurants.menu.revisions")
                .description("Total number of restaurant menu revisions")
                .register(registry);

        ticketPreparationTime = Timer.builder("ftgo.restaurants.ticket.preparation.time")
                .description("Time from ticket acceptance to ready-for-pickup")
                .register(registry);
    }

    public Counter getRestaurantsCreated() { return restaurantsCreated; }
    public Counter getTicketsCreated() { return ticketsCreated; }
    public Counter getTicketsAccepted() { return ticketsAccepted; }
    public Counter getTicketsPreparing() { return ticketsPreparing; }
    public Counter getTicketsReadyForPickup() { return ticketsReadyForPickup; }
    public Counter getMenuRevisions() { return menuRevisions; }
    public Timer getTicketPreparationTime() { return ticketPreparationTime; }
}
