package com.ftgo.test.builders;

import com.ftgo.test.assertions.OrderAssert;
import com.ftgo.test.fixtures.FtgoMothers;
import com.ftgo.test.fixtures.OrderFixture;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.jupiter.api.Test;

import static com.ftgo.test.assertions.MoneyAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

class OrderBuilderTest {

    @Test
    void anOrder_withDefaults_producesSingleLineChickenVindalooOrder() {
        OrderFixture order = OrderBuilder.anOrder().build();

        OrderAssert.assertThat(order)
                .hasState("APPROVED")
                .hasLineItemCount(1);
    }

    @Test
    void anOrder_withMultipleLineItems_sumsOrderTotal() {
        OrderFixture order = OrderBuilder.anOrder()
                .withLineItem("soup", "Soup", new Money("3.00"), 2)
                .withLineItem("salad", "Salad", new Money("5.00"), 1)
                .build();

        assertThat(order.orderTotal()).isEqualToAmount("11.00");
    }

    @Test
    void anOrder_withConsumerAndRestaurant_setsOwnership() {
        OrderFixture order = OrderBuilder.anOrder()
                .withConsumer(42L)
                .withRestaurant(7L)
                .build();

        OrderAssert.assertThat(order).belongsToConsumer(42L);
        assertThat(order.restaurantId()).isEqualTo(7L);
    }

    @Test
    void chickenVindalooOrder_matchesLegacyMonolithFixture() {
        OrderFixture order = FtgoMothers.chickenVindalooOrder().build();

        OrderAssert.assertThat(order)
                .belongsToConsumer(FtgoMothers.CONSUMER_ID)
                .hasState("APPROVED")
                .hasTotal(FtgoMothers.CHICKEN_VINDALOO_PRICE.multiply(FtgoMothers.CHICKEN_VINDALOO_QUANTITY));
    }
}
