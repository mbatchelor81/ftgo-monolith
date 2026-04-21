package com.ftgo.example.domain;

import com.ftgo.test.builders.OrderBuilder;
import com.ftgo.test.fixtures.OrderFixture;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ftgo.test.assertions.MoneyAssert.assertThat;
import static com.ftgo.test.assertions.OrderAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FTGO unit-test template.
 *
 * <p>ArchitectureNotes
 * ------------------
 * A unit test in FTGO:
 *   - Exercises a single class — here, the (placeholder) {@code OrderService}.
 *   - Mocks every collaborator (repositories, clocks, HTTP clients, …).
 *   - Does NOT boot a Spring context, hit a database, or open a socket.
 *   - Runs in milliseconds and contributes to the 2-minute CI budget.
 *
 * <p>When to choose this tier
 * -------------------------
 * See {@code docs/testing/when-to-write-which-test.md}. In short: any
 * behaviour reproducible with a Mockito mock belongs here.
 *
 * <p>Replace {@code OrderService} / {@code OrderRepository} with the
 * class and its collaborator in your service, and delete any test
 * methods that don't apply. The nested classes showcase three common
 * patterns: happy path, validation / error branch, and parameterized.
 */
@ExtendWith(MockitoExtension.class)
class UnitTestTemplate {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        // Add any per-test fixture setup here. Use builders from
        // com.ftgo.test.builders — avoid raw constructors.
    }

    @Nested
    class CreateOrder {

        @Test
        void withValidRequest_persistsAndReturnsApprovedOrder() {
            // Arrange
            OrderFixture order = OrderBuilder.anOrder()
                    .withConsumer(42L)
                    .withRestaurant(7L)
                    .withLineItem("vindaloo", "Chicken Vindaloo", new Money("12.50"), 2)
                    .build();
            when(orderRepository.save(any(OrderFixture.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            OrderFixture saved = orderService.create(order);

            // Assert
            assertThat(saved).hasState("APPROVED").belongsToConsumer(42L);
            assertThat(saved.orderTotal()).isEqualToAmount("25.00");
            verify(orderRepository).save(any(OrderFixture.class));
        }
    }

    @Nested
    class Validation {

        @ParameterizedTest(name = "quantity {0} rejected")
        @ValueSource(ints = {0, -1, -100})
        void withNonPositiveQuantity_throws(int quantity) {
            OrderFixture order = OrderBuilder.anOrder()
                    .withLineItem("vindaloo", "Chicken Vindaloo", new Money("12.50"), quantity)
                    .build();

            assertThatThrownBy(() -> orderService.create(order))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("quantity");
        }
    }

    // ------------------------------------------------------------------
    // Placeholder types — replace with the real classes in your service.
    // ------------------------------------------------------------------

    interface OrderRepository {
        OrderFixture save(OrderFixture order);
    }

    static class OrderService {
        private final OrderRepository repository;

        OrderService(OrderRepository repository) {
            this.repository = repository;
        }

        OrderFixture create(OrderFixture order) {
            order.lineItems().forEach(item -> {
                if (item.quantity() <= 0) {
                    throw new IllegalArgumentException("Line item quantity must be positive");
                }
            });
            return repository.save(order);
        }
    }
}
