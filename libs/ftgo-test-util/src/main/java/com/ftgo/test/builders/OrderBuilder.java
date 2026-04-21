package com.ftgo.test.builders;

import com.ftgo.test.fixtures.OrderFixture;
import com.ftgo.test.fixtures.OrderFixture.LineItemFixture;
import net.chrisrichardson.ftgo.common.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for {@link OrderFixture} test fixtures.
 *
 * <p>Defaults to a single-line "Chicken Vindaloo" order in the
 * {@code APPROVED} state — the most common starting point for the
 * legacy {@code OrderControllerTest} and its descendants. Tests only
 * need to call mutators for the attributes that actually drive the
 * assertion.
 *
 * <p>{@link #build()} recomputes {@code orderTotal} from the line items
 * each time, so tests never have to keep totals in sync with quantities
 * by hand.
 *
 * <pre>{@code
 * OrderFixture order = OrderBuilder.anOrder()
 *     .withConsumer(42L)
 *     .withRestaurant(7L)
 *     .withLineItem("vindaloo", "Chicken Vindaloo", new Money("12.50"), 2)
 *     .build();
 * }</pre>
 */
public final class OrderBuilder {

    private Long id = 99L;
    private Long consumerId = 1L;
    private Long restaurantId = 1L;
    private String state = "APPROVED";
    private final List<LineItemFixture> lineItems = new ArrayList<>();

    private OrderBuilder() {
    }

    public static OrderBuilder anOrder() {
        return new OrderBuilder();
    }

    public OrderBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public OrderBuilder withConsumer(Long consumerId) {
        this.consumerId = consumerId;
        return this;
    }

    public OrderBuilder withRestaurant(Long restaurantId) {
        this.restaurantId = restaurantId;
        return this;
    }

    public OrderBuilder withState(String state) {
        this.state = state;
        return this;
    }

    public OrderBuilder withLineItem(String menuItemId, String name, Money price, int quantity) {
        this.lineItems.add(new LineItemFixture(menuItemId, name, price, quantity));
        return this;
    }

    public OrderFixture build() {
        if (lineItems.isEmpty()) {
            lineItems.add(new LineItemFixture("vindaloo", "Chicken Vindaloo", new Money("12.50"), 1));
        }
        return new OrderFixture(id, consumerId, restaurantId, state, List.copyOf(lineItems), computeTotal());
    }

    private Money computeTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (LineItemFixture item : lineItems) {
            total = total.add(new BigDecimal(item.price().asString()).multiply(BigDecimal.valueOf(item.quantity())));
        }
        return new Money(total);
    }
}
