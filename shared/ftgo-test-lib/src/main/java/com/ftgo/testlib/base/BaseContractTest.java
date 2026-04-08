package com.ftgo.testlib.base;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for consumer-driven contract tests.
 *
 * <p>Provides:</p>
 * <ul>
 *   <li>Rest-Assured MockMvc integration for lightweight contract verification</li>
 *   <li>Mockito extension for mocking service layer dependencies</li>
 *   <li>"contract" tag for selective test execution</li>
 * </ul>
 *
 * <p>Usage with Spring Cloud Contract:</p>
 * <pre>{@code
 * @WebMvcTest(OrderController.class)
 * class OrderContractTest extends BaseContractTest {
 *
 *     @Autowired
 *     private MockMvc mockMvc;
 *
 *     @MockBean
 *     private OrderService orderService;
 *
 *     @BeforeEach
 *     void setup() {
 *         initMockMvc(mockMvc);
 *         // Stub service responses for contract verification
 *         when(orderService.findById(1L)).thenReturn(Optional.of(testOrder));
 *     }
 * }
 * }</pre>
 *
 * <p>Usage with Pact:</p>
 * <pre>{@code
 * @ExtendWith(PactConsumerTestExt.class)
 * class OrderServicePactTest extends BaseContractTest {
 *     // Define consumer pacts
 * }
 * }</pre>
 *
 * <p><strong>Guidelines:</strong></p>
 * <ul>
 *   <li>Verify API contracts between services without full integration</li>
 *   <li>Mock the service layer; test only the controller + serialization</li>
 *   <li>Use for verifying request/response schemas and status codes</li>
 *   <li>Run as part of the build pipeline to catch breaking API changes</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@Tag("contract")
public abstract class BaseContractTest {

    /**
     * Initialize Rest-Assured MockMvc with the given {@link MockMvc} instance.
     * Call this in {@code @BeforeEach} after injecting MockMvc.
     */
    protected void initMockMvc(MockMvc mockMvc) {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }
}
