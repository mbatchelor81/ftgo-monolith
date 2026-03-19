package com.ftgo.test.templates;

/**
 * Template / reference for writing contract tests in the FTGO project.
 * Contract tests verify that service-to-service API expectations are met.
 *
 * Conventions:
 * - Provider side: Use Spring Cloud Contract verifier
 * - Consumer side: Use Spring Cloud Contract stub runner
 * - Name: {ServiceName}ContractTest.java in src/contractTest/java
 * - Contracts defined as Groovy DSL or YAML under src/contractTest/resources/contracts/
 *
 * Provider-side Example (producer of the API):
 * <pre>
 * {@code
 * @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
 * @AutoConfigureMockMvc
 * class OrderServiceContractVerifierTest {
 *
 *     @Autowired
 *     private MockMvc mockMvc;
 *
 *     @Test
 *     void getOrder_matchesContract() throws Exception {
 *         mockMvc.perform(get("/orders/1")
 *                         .accept(MediaType.APPLICATION_JSON))
 *                 .andExpect(status().isOk())
 *                 .andExpect(jsonPath("$.orderId").isNumber())
 *                 .andExpect(jsonPath("$.state").isString())
 *                 .andExpect(jsonPath("$.consumerId").isNumber())
 *                 .andExpect(jsonPath("$.restaurantId").isNumber());
 *     }
 * }
 * }
 * </pre>
 *
 * Consumer-side Example (consumer of another service's API):
 * <pre>
 * {@code
 * @SpringBootTest
 * @AutoConfigureStubRunner(
 *         ids = "com.ftgo:ftgo-restaurant-service:+:stubs:8090",
 *         stubsMode = StubRunnerProperties.StubsMode.LOCAL)
 * class OrderServiceConsumerContractTest {
 *
 *     @Autowired
 *     private RestaurantServiceClient restaurantClient;
 *
 *     @Test
 *     void getRestaurant_returnsExpectedShape() {
 *         var restaurant = restaurantClient.getRestaurant(1L);
 *         assertThat(restaurant).isNotNull();
 *         assertThat(restaurant.getName()).isNotBlank();
 *     }
 * }
 * }
 * </pre>
 */
public final class ContractTestTemplate {
    private ContractTestTemplate() {
        // Reference class — not meant to be instantiated
    }
}
