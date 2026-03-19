package com.ftgo.test.templates;

/**
 * Template / reference for writing REST API tests using Rest-Assured style
 * with Spring Boot Test's TestRestTemplate.
 *
 * Conventions:
 * - Use @SpringBootTest with RANDOM_PORT
 * - Use TestRestTemplate or WebTestClient for HTTP calls
 * - Test all HTTP verbs, status codes, error responses
 * - Verify response headers (Content-Type, Location, etc.)
 * - Name: {Resource}ApiTest.java in src/integrationTest/java
 *
 * Example:
 * <pre>
 * {@code
 * @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 * class OrderApiTest extends TestContainersConfiguration {
 *
 *     @Autowired
 *     private TestRestTemplate restTemplate;
 *
 *     @Test
 *     void createOrder_withValidPayload_returns201() {
 *         var request = new HttpEntity<>(
 *                 Map.of("consumerId", 1, "restaurantId", 1,
 *                        "lineItems", List.of(Map.of("menuItemId", "item-1", "quantity", 2))),
 *                 jsonHeaders());
 *
 *         var response = restTemplate.postForEntity("/api/orders", request, String.class);
 *
 *         assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
 *         assertThat(response.getHeaders().getContentType())
 *                 .isEqualTo(MediaType.APPLICATION_JSON);
 *     }
 *
 *     @Test
 *     void createOrder_withMissingFields_returns400() {
 *         var request = new HttpEntity<>(Map.of(), jsonHeaders());
 *
 *         var response = restTemplate.postForEntity("/api/orders", request, String.class);
 *
 *         assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
 *     }
 *
 *     @Test
 *     void getOrder_whenUnauthorized_returns401() {
 *         var response = restTemplate.getForEntity("/api/orders/1", String.class);
 *         assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
 *     }
 *
 *     private HttpHeaders jsonHeaders() {
 *         var headers = new HttpHeaders();
 *         headers.setContentType(MediaType.APPLICATION_JSON);
 *         return headers;
 *     }
 * }
 * }
 * </pre>
 */
public final class ApiTestTemplate {
    private ApiTestTemplate() {
        // Reference class — not meant to be instantiated
    }
}
