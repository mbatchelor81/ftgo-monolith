package com.ftgo.testlib.templates;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Template for unit tests following JUnit 5 + Mockito + AssertJ patterns.
 *
 * <h2>How to use this template</h2>
 *
 * <ol>
 *   <li>Copy this file into your service's {@code src/test/java} directory
 *   <li>Rename the class to match your service (e.g., {@code OrderServiceTest})
 *   <li>Uncomment and adapt the mock/inject annotations
 *   <li>Replace placeholder assertions with real test logic
 * </ol>
 *
 * <h2>Conventions</h2>
 *
 * <ul>
 *   <li>Use {@code @ExtendWith(MockitoExtension.class)} — no Spring context needed
 *   <li>Use {@code @Nested} classes to group tests by method
 *   <li>Name tests: {@code methodName_condition_expectedResult}
 *   <li>Follow Arrange-Act-Assert pattern
 *   <li>Use AssertJ's {@code assertThat} over JUnit assertions
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * @ExtendWith(MockitoExtension.class)
 * @DisplayName("OrderService")
 * class OrderServiceTest {
 *
 *     @Mock private OrderRepository orderRepository;
 *     @Mock private ConsumerRepository consumerRepository;
 *     @InjectMocks private OrderService orderService;
 *
 *     @Nested
 *     @DisplayName("createOrder")
 *     class CreateOrder {
 *         @Test
 *         @DisplayName("should create order with valid input")
 *         void createOrder_withValidInput_returnsOrder() {
 *             // Arrange
 *             when(consumerRepository.findById(1L))
 *                 .thenReturn(Optional.of(ConsumerBuilder.aConsumer().build()));
 *             when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));
 *
 *             // Act
 *             var result = orderService.createOrder(request);
 *
 *             // Assert
 *             assertThat(result).isNotNull();
 *             verify(orderRepository).save(any(Order.class));
 *         }
 *     }
 * }
 * }</pre>
 *
 * @see org.junit.jupiter.api.extension.ExtendWith
 * @see org.mockito.junit.jupiter.MockitoExtension
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UnitTestTemplate — copy and rename for your service")
@SuppressWarnings(
        "checkstyle:MethodName") // Template uses test naming convention: method_condition_result
public class UnitTestTemplate {

    // --- Step 1: Declare mocks for dependencies ---
    // @Mock
    // private YourRepository yourRepository;

    // --- Step 2: Inject mocks into the class under test ---
    // @InjectMocks
    // private YourService yourService;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create entity with valid input")
        void create_withValidInput_createsEntity() {
            // Arrange
            // var request = new CreateRequest("name");
            // when(yourRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // Act
            // var result = yourService.create(request);

            // Assert
            // assertThat(result).isNotNull();
            // verify(yourRepository).save(any());
            assertThat(true).isTrue(); // Placeholder
        }

        @Test
        @DisplayName("should reject null input")
        void create_withNullInput_throwsException() {
            // assertThatThrownBy(() -> yourService.create(null))
            //     .isInstanceOf(IllegalArgumentException.class);
            assertThat(true).isTrue(); // Placeholder
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return entity when it exists")
        void findById_whenExists_returnsEntity() {
            // Arrange
            // when(yourRepository.findById(1L)).thenReturn(Optional.of(entity));

            // Act
            // var result = yourService.findById(1L);

            // Assert
            // assertThat(result).isPresent();
            assertThat(true).isTrue(); // Placeholder
        }

        @Test
        @DisplayName("should return empty when not found")
        void findById_whenNotExists_returnsEmpty() {
            // when(yourRepository.findById(999L)).thenReturn(Optional.empty());
            // var result = yourService.findById(999L);
            // assertThat(result).isEmpty();
            assertThat(true).isTrue(); // Placeholder
        }
    }
}
