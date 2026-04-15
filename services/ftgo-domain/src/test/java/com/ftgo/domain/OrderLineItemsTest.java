package com.ftgo.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.ftgo.common.Money;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link OrderLineItems} demonstrating order total calculation and revision logic.
 */
@DisplayName("OrderLineItems")
class OrderLineItemsTest {

    private OrderLineItems orderLineItems;

    @BeforeEach
    void setUp() {
        orderLineItems =
                new OrderLineItems(
                        List.of(
                                new OrderLineItem("item-1", "Vindaloo", new Money(10), 2),
                                new OrderLineItem("item-2", "Biryani", new Money(15), 1)));
    }

    @Nested
    @DisplayName("orderTotal")
    class OrderTotal {

        @Test
        @DisplayName("should sum all line item totals")
        void orderTotal_shouldSumLineItems() {
            // (10 * 2) + (15 * 1) = 35
            assertThat(orderLineItems.orderTotal()).isEqualTo(new Money(35));
        }

        @Test
        @DisplayName("should return zero for empty line items")
        void orderTotal_withEmptyItems_shouldReturnZero() {
            var empty = new OrderLineItems(List.of());
            assertThat(empty.orderTotal()).isEqualTo(Money.ZERO);
        }
    }

    @Nested
    @DisplayName("findOrderLineItem")
    class FindOrderLineItem {

        @Test
        @DisplayName("should find line item by menu item ID")
        void findOrderLineItem_withExistingId_returnsItem() {
            OrderLineItem found = orderLineItems.findOrderLineItem("item-1");
            assertThat(found.getName()).isEqualTo("Vindaloo");
            assertThat(found.getQuantity()).isEqualTo(2);
        }
    }
}
