package com.ftgo.test.templates;

/**
 * Template / reference for writing unit tests in the FTGO project.
 * Copy this class and rename it for your specific service test.
 *
 * Conventions:
 * - Use JUnit 5 (@ExtendWith, @Test from org.junit.jupiter.api)
 * - Use Mockito for mocking (@Mock, @InjectMocks)
 * - Use AssertJ for assertions (assertThat, assertThatThrownBy)
 * - Name: {ClassName}Test.java in src/test/java
 * - Method naming: {methodUnderTest}_{scenario}_{expectedResult}
 * - AAA pattern: Arrange, Act, Assert with blank line separators
 *
 * Example:
 * <pre>
 * {@code
 * @ExtendWith(MockitoExtension.class)
 * class OrderServiceTest {
 *
 *     @Mock
 *     private OrderRepository orderRepository;
 *
 *     @Mock
 *     private ConsumerService consumerService;
 *
 *     @InjectMocks
 *     private OrderService orderService;
 *
 *     @Test
 *     void createOrder_withValidRequest_returnsApprovedOrder() {
 *         // Arrange
 *         var consumer = ConsumerBuilder.aConsumer().withId(1L).build();
 *         when(consumerService.findById(1L)).thenReturn(Optional.of(consumer));
 *         when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
 *
 *         // Act
 *         var result = orderService.createOrder(1L, 1L, List.of());
 *
 *         // Assert
 *         assertThat(result).isNotNull();
 *         assertThat(result.getState()).isEqualTo(OrderState.APPROVED);
 *         verify(orderRepository).save(any());
 *     }
 *
 *     @Test
 *     void createOrder_withInvalidConsumer_throwsException() {
 *         // Arrange
 *         when(consumerService.findById(999L)).thenReturn(Optional.empty());
 *
 *         // Act & Assert
 *         assertThatThrownBy(() -> orderService.createOrder(999L, 1L, List.of()))
 *                 .isInstanceOf(ConsumerNotFoundException.class)
 *                 .hasMessageContaining("999");
 *     }
 *
 *     @Nested
 *     class WhenOrderExists {
 *         private Order existingOrder;
 *
 *         @BeforeEach
 *         void setUp() {
 *             existingOrder = OrderBuilder.anOrder().withId(1L).build();
 *             when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
 *         }
 *
 *         @Test
 *         void cancel_returnsSuccessfully() { ... }
 *
 *         @Test
 *         void revise_updatesLineItems() { ... }
 *     }
 * }
 * }
 * </pre>
 */
public final class UnitTestTemplate {
    private UnitTestTemplate() {
        // Reference class — not meant to be instantiated
    }
}
