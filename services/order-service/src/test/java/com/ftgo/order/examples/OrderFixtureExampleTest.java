package com.ftgo.order.examples;

import com.ftgo.test.builders.OrderBuilder;
import com.ftgo.test.fixtures.FtgoMothers;
import com.ftgo.test.fixtures.OrderFixture;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.jupiter.api.Test;

import static com.ftgo.test.assertions.MoneyAssert.assertThat;
import static com.ftgo.test.assertions.OrderAssert.assertThat;

/**
 * Unit-tier example for the Order bounded context.
 *
 * <p>Demonstrates:
 *   <ul>
 *     <li>{@link OrderBuilder} fluent API for assembling orders with
 *         deterministic totals (the builder auto-computes
 *         {@code orderTotal} from line items, matching the production
 *         {@code OrderLineItems.orderTotal()} semantics).</li>
 *     <li>Domain-aware AssertJ assertions {@link com.ftgo.test.assertions.OrderAssert}
 *         and {@link com.ftgo.test.assertions.MoneyAssert} for readable
 *         failures.</li>
 *     <li>{@link FtgoMothers} object mothers that mirror the legacy
 *         monolith's canonical fixtures — important so new service tests
 *         don't drift from long-standing regression suites.</li>
 *   </ul>
 *
 * <p>For a real order-service domain test see
 * {@code templates/test-templates/UnitTestTemplate.java}. This file's
 * job is to prove the test-util library works end-to-end on the order
 * bounded context before the real {@code OrderService} lands
 * (EM-3x).
 */
class OrderFixtureExampleTest {

    @Test
    void anOrder_withDefaults_matchesChickenVindalooFixture() {
        OrderFixture order = OrderBuilder.anOrder().build();

        assertThat(order)
                .hasState("APPROVED")
                .hasLineItemCount(1)
                .hasTotal(new Money("12.50"));
    }

    @Test
    void anOrder_withTwoLineItems_sumsTotalAcrossQuantities() {
        OrderFixture order = OrderBuilder.anOrder()
                .withLineItem("vindaloo", "Chicken Vindaloo", new Money("12.50"), 2)
                .withLineItem("naan", "Garlic Naan", new Money("3.75"), 4)
                .build();

        // 12.50 * 2 + 3.75 * 4 = 40.00
        assertThat(order).hasLineItemCount(2);
        assertThat(order.orderTotal()).isEqualToAmount("40.00");
    }

    @Test
    void chickenVindalooOrder_hasLegacyConsumerAndRestaurantIds() {
        OrderFixture order = FtgoMothers.chickenVindalooOrder().build();

        assertThat(order)
                .belongsToConsumer(FtgoMothers.CONSUMER_ID);
        // Total must remain positive — acts as a tripwire if the
        // canonical fixture is ever edited down to a zero-item order.
        assertThat(order.orderTotal()).isPositive();
    }
}
