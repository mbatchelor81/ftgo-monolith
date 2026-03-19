package com.ftgo.test.templates;

/**
 * Template / reference for writing integration tests in the FTGO project.
 * Copy this class and rename it for your specific service integration test.
 *
 * Conventions:
 * - Extend {@code TestContainersConfiguration} for MySQL support
 * - Use @SpringBootTest with RANDOM_PORT
 * - Use TestRestTemplate for HTTP calls
 * - Name: {ClassName}IntegrationTest.java in src/integrationTest/java
 * - Each test manages its own data — no shared fixtures
 * - Use @Transactional for database cleanup where applicable
 *
 * Example:
 * <pre>
 * {@code
 * @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 * class OrderControllerIntegrationTest extends TestContainersConfiguration {
 *
 *     @Autowired
 *     private TestRestTemplate restTemplate;
 *
 *     @Autowired
 *     private OrderRepository orderRepository;
 *
 *     @Test
 *     void createOrder_returns201WithLocationHeader() {
 *         // Arrange
 *         var request = new CreateOrderRequest(1L, 1L, List.of(
 *                 new CreateOrderRequest.LineItem("item-1", 2)));
 *
 *         // Act
 *         var response = restTemplate.postForEntity("/orders", request, OrderResponse.class);
 *
 *         // Assert
 *         assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
 *         assertThat(response.getHeaders().getLocation()).isNotNull();
 *         assertThat(response.getBody()).isNotNull();
 *         assertThat(response.getBody().getOrderId()).isPositive();
 *     }
 *
 *     @Test
 *     void getOrder_whenNotFound_returns404() {
 *         var response = restTemplate.getForEntity("/orders/99999", OrderResponse.class);
 *         assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
 *     }
 *
 *     @Test
 *     void listOrders_returnsPaginatedResults() {
 *         // Arrange — seed some orders
 *         orderRepository.saveAll(List.of(...));
 *
 *         // Act
 *         var response = restTemplate.getForEntity(
 *                 "/orders?page=0&size=10", OrderListResponse.class);
 *
 *         // Assert
 *         assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
 *         assertThat(response.getBody().getContent()).hasSizeGreaterThanOrEqualTo(1);
 *     }
 * }
 * }
 * </pre>
 */
public final class IntegrationTestTemplate {
    private IntegrationTestTemplate() {
        // Reference class — not meant to be instantiated
    }
}
