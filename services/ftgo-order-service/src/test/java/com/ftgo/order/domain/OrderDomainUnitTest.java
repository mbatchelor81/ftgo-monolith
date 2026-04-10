package com.ftgo.order.domain;

import com.ftgo.testlib.base.BaseUnitTest;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.ftgo.testlib.assertion.FtgoAssertions.assertThat;
import static com.ftgo.testlib.builder.TestBuilders.order;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test template for Order domain logic.
 * Demonstrates testing state machine transitions, business rules,
 * and domain invariants using test builders and custom assertions.
 */
@DisplayName("Order Domain")
class OrderDomainUnitTest extends BaseUnitTest {

    @Nested
    @DisplayName("Order creation")
    class Creation {

        @Test
        void createOrder_withValidInput_isApproved() {
            // Arrange & Act
            Order order = order()
                    .withConsumerId(42L)
                    .build();

            // Assert
            assertThat(order)
                    .isApproved()
                    .belongsToConsumer(42L)
                    .hasLineItemCount(1);
        }
    }

    @Nested
    @DisplayName("Order cancellation")
    class Cancellation {

        @Test
        void cancel_whenApproved_transitionsToCancelled() {
            // Arrange
            Order order = order().build();

            // Act
            order.cancel();

            // Assert
            assertThat(order).isCancelled();
        }

        @Test
        void cancel_whenNotApproved_throwsException() {
            // Arrange
            Order order = order().build();
            order.acceptTicket(LocalDateTime.now().plusHours(1));

            // Act & Assert
            assertThatThrownBy(order::cancel)
                    .isInstanceOf(UnsupportedStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("Order lifecycle")
    class Lifecycle {

        @Test
        void acceptTicket_whenApproved_transitionsToAccepted() {
            // Arrange
            Order order = order().build();
            LocalDateTime readyBy = LocalDateTime.now().plusHours(1);

            // Act
            order.acceptTicket(readyBy);

            // Assert
            assertThat(order).isAccepted();
        }

        @Test
        void notePreparing_whenAccepted_transitionsToPreparing() {
            // Arrange
            Order order = order().build();
            order.acceptTicket(LocalDateTime.now().plusHours(1));

            // Act
            order.notePreparing();

            // Assert
            assertThat(order).isPreparing();
        }

        @Test
        void fullLifecycle_fromApprovedToDelivered() {
            // Arrange
            Order order = order().build();

            // Act — walk through the full state machine
            order.acceptTicket(LocalDateTime.now().plusHours(1));
            order.notePreparing();
            order.noteReadyForPickup();
            order.notePickedUp();
            order.noteDelivered();

            // Assert
            assertThat(order).isDelivered();
        }

        @Test
        void notePreparing_whenApproved_throwsException() {
            // Arrange
            Order order = order().build();

            // Act & Assert — cannot skip ACCEPTED state
            assertThatThrownBy(order::notePreparing)
                    .isInstanceOf(UnsupportedStateTransitionException.class);
        }
    }
}
