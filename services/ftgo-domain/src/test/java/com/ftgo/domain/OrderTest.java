package com.ftgo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ftgo.common.Money;
import com.ftgo.common.UnsupportedStateTransitionException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Order} entity demonstrating state machine transitions.
 *
 * <p>These tests verify:
 *
 * <ul>
 *   <li>Valid state transitions through the order lifecycle
 *   <li>Rejection of invalid state transitions
 *   <li>Order total calculation
 *   <li>Courier scheduling
 * </ul>
 */
@DisplayName("Order")
class OrderTest {

    private Restaurant restaurant;
    private List<OrderLineItem> lineItems;
    private Order order;

    @BeforeEach
    void setUp() {
        restaurant =
                new Restaurant(
                        "Ajanta",
                        null,
                        new RestaurantMenu(
                                List.of(new MenuItem("item-1", "Vindaloo", new Money(10)))));
        lineItems = List.of(new OrderLineItem("item-1", "Vindaloo", new Money(10), 2));
        order = new Order(1L, restaurant, lineItems);
    }

    @Test
    @DisplayName("new order should be in APPROVED state")
    void newOrder_shouldBeApproved() {
        assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
    }

    @Test
    @DisplayName("should have correct consumer ID")
    void newOrder_shouldHaveConsumerId() {
        assertThat(order.getConsumerId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("should calculate order total from line items")
    void getOrderTotal_shouldSumLineItems() {
        // 10 * 2 = 20
        assertThat(order.getOrderTotal()).isEqualTo(new Money(20));
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("should set state to CANCELLED when order is APPROVED")
        void cancel_whenApproved_setsStateToCancelled() {
            order.cancel();
            assertThat(order.getOrderState()).isEqualTo(OrderState.CANCELLED);
        }

        @Test
        @DisplayName("should throw when order is not APPROVED")
        void cancel_whenNotApproved_throwsException() {
            order.acceptTicket(LocalDateTime.now().plusHours(1));

            assertThatThrownBy(() -> order.cancel())
                    .isInstanceOf(UnsupportedStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("acceptTicket")
    class AcceptTicket {

        @Test
        @DisplayName("should transition to ACCEPTED when APPROVED")
        void acceptTicket_whenApproved_transitionsToAccepted() {
            order.acceptTicket(LocalDateTime.now().plusHours(1));
            assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
        }

        @Test
        @DisplayName("should reject when readyBy is not in the future")
        void acceptTicket_withPastReadyBy_throwsException() {
            assertThatThrownBy(() -> order.acceptTicket(LocalDateTime.now().minusHours(1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("readyBy");
        }

        @Test
        @DisplayName("should throw when not in APPROVED state")
        void acceptTicket_whenNotApproved_throwsException() {
            order.acceptTicket(LocalDateTime.now().plusHours(1));

            assertThatThrownBy(() -> order.acceptTicket(LocalDateTime.now().plusHours(2)))
                    .isInstanceOf(UnsupportedStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("notePreparing")
    class NotePreparing {

        @Test
        @DisplayName("should transition to PREPARING when ACCEPTED")
        void notePreparing_whenAccepted_transitionsToPreparing() {
            order.acceptTicket(LocalDateTime.now().plusHours(1));
            order.notePreparing();
            assertThat(order.getOrderState()).isEqualTo(OrderState.PREPARING);
        }

        @Test
        @DisplayName("should throw when not in ACCEPTED state")
        void notePreparing_whenNotAccepted_throwsException() {
            assertThatThrownBy(() -> order.notePreparing())
                    .isInstanceOf(UnsupportedStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("noteReadyForPickup")
    class NoteReadyForPickup {

        @Test
        @DisplayName("should transition to READY_FOR_PICKUP when PREPARING")
        void noteReadyForPickup_whenPreparing_transitionsToReady() {
            order.acceptTicket(LocalDateTime.now().plusHours(1));
            order.notePreparing();
            order.noteReadyForPickup();
            assertThat(order.getOrderState()).isEqualTo(OrderState.READY_FOR_PICKUP);
        }

        @Test
        @DisplayName("should throw when not in PREPARING state")
        void noteReadyForPickup_whenNotPreparing_throwsException() {
            assertThatThrownBy(() -> order.noteReadyForPickup())
                    .isInstanceOf(UnsupportedStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("notePickedUp")
    class NotePickedUp {

        @Test
        @DisplayName("should transition to PICKED_UP when READY_FOR_PICKUP")
        void notePickedUp_whenReady_transitionsToPickedUp() {
            order.acceptTicket(LocalDateTime.now().plusHours(1));
            order.notePreparing();
            order.noteReadyForPickup();
            order.notePickedUp();
            assertThat(order.getOrderState()).isEqualTo(OrderState.PICKED_UP);
        }

        @Test
        @DisplayName("should throw when not in READY_FOR_PICKUP state")
        void notePickedUp_whenNotReady_throwsException() {
            assertThatThrownBy(() -> order.notePickedUp())
                    .isInstanceOf(UnsupportedStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("noteDelivered")
    class NoteDelivered {

        @Test
        @DisplayName("should transition to DELIVERED when PICKED_UP")
        void noteDelivered_whenPickedUp_transitionsToDelivered() {
            order.acceptTicket(LocalDateTime.now().plusHours(1));
            order.notePreparing();
            order.noteReadyForPickup();
            order.notePickedUp();
            order.noteDelivered();
            assertThat(order.getOrderState()).isEqualTo(OrderState.DELIVERED);
        }

        @Test
        @DisplayName("should throw when not in PICKED_UP state")
        void noteDelivered_whenNotPickedUp_throwsException() {
            assertThatThrownBy(() -> order.noteDelivered())
                    .isInstanceOf(UnsupportedStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("schedule")
    class Schedule {

        @Test
        @DisplayName("should assign courier to the order")
        void schedule_shouldAssignCourier() {
            var courier =
                    new Courier(
                            new com.ftgo.common.PersonName("Alex", "Courier"),
                            new com.ftgo.common.Address(
                                    "1 Main St", null, "Oakland", "CA", "94609"));
            order.schedule(courier);
            assertThat(order.getAssignedCourier()).isSameAs(courier);
        }
    }

    @Nested
    @DisplayName("full lifecycle")
    class FullLifecycle {

        @Test
        @DisplayName("should complete full order lifecycle from APPROVED to DELIVERED")
        void fullLifecycle_shouldTransitionThroughAllStates() {
            assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);

            order.acceptTicket(LocalDateTime.now().plusHours(1));
            assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);

            order.notePreparing();
            assertThat(order.getOrderState()).isEqualTo(OrderState.PREPARING);

            order.noteReadyForPickup();
            assertThat(order.getOrderState()).isEqualTo(OrderState.READY_FOR_PICKUP);

            order.notePickedUp();
            assertThat(order.getOrderState()).isEqualTo(OrderState.PICKED_UP);

            order.noteDelivered();
            assertThat(order.getOrderState()).isEqualTo(OrderState.DELIVERED);
        }
    }
}
