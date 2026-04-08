package com.ftgo.testlib.base;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Base class for unit tests.
 *
 * <p>Provides:</p>
 * <ul>
 *   <li>Mockito extension for {@code @Mock} and {@code @InjectMocks} support</li>
 *   <li>"unit" tag for selective test execution</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * class OrderServiceTest extends BaseUnitTest {
 *
 *     @Mock
 *     private OrderRepository orderRepository;
 *
 *     @InjectMocks
 *     private OrderService orderService;
 *
 *     @Test
 *     void createOrder_withValidInput_returnsOrder() {
 *         // Arrange - Act - Assert
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Guidelines:</strong></p>
 * <ul>
 *   <li>Mock all external dependencies (repositories, other services, clients)</li>
 *   <li>Test one behavior per method</li>
 *   <li>Name tests: {@code methodName_condition_expectedResult}</li>
 *   <li>Target execution time: &lt; 2 minutes for the entire unit test suite</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
public abstract class BaseUnitTest {
    // Intentionally empty — extensions and tags are inherited by subclasses
}
