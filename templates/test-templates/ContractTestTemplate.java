package com.ftgo.example.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * FTGO contract-test <strong>base class</strong> template.
 *
 * <p>ArchitectureNotes
 * ------------------
 * Contract tests in FTGO use <strong>Spring Cloud Contract</strong>. The
 * plugin generates one verifier class per Groovy contract under
 * {@code src/test/resources/contracts/<consumer>/…}. Those generated
 * classes extend the base class you define here.
 *
 * <p>This file is the <strong>producer-side</strong> base. It has two
 * responsibilities:
 *   <ol>
 *     <li>Boot a {@code @SpringBootTest} context that wires the
 *         controllers the contracts will call.</li>
 *     <li>Configure Rest-Assured MockMvc so the verifier invokes those
 *         controllers directly — no network, but full request / response
 *         serialisation.</li>
 *   </ol>
 *
 * <p>Wiring the Spring Cloud Contract plugin
 * ----------------------------------------
 * In the producing service's {@code build.gradle}:
 *
 * <pre>{@code
 * plugins {
 *     id 'org.springframework.cloud.contract' version '4.1.1'
 * }
 *
 * dependencies {
 *     testImplementation 'org.springframework.cloud:spring-cloud-starter-contract-verifier'
 * }
 *
 * contracts {
 *     baseClassForTests = 'com.ftgo.order.contract.BaseContract'
 *     packageWithBaseClasses = 'com.ftgo.order.contract'
 *     contractsDslDir = file('src/test/resources/contracts')
 * }
 * }</pre>
 *
 * <p>See {@code docs/testing/contract-testing.md} for the full workflow
 * (producer contracts → stub JAR → consumer stub-runner wiring).
 *
 * <p>Example Groovy contract
 * ------------------------
 * <pre>{@code
 * // src/test/resources/contracts/consumer-service/shouldReturnOrderById.groovy
 * package contracts.consumer
 *
 * import org.springframework.cloud.contract.spec.Contract
 *
 * Contract.make {
 *     description "should return an order by id"
 *     request {
 *         method GET()
 *         url "/orders/99"
 *     }
 *     response {
 *         status OK()
 *         headers { contentType applicationJson() }
 *         body([
 *             orderId:    99,
 *             state:      "APPROVED",
 *             orderTotal: "62.50"
 *         ])
 *     }
 * }
 * }</pre>
 */
@SpringBootTest
public abstract class ContractTestTemplate {

    // Autowire the controller(s) the contracts will hit. The Spring Cloud
    // Contract verifier does not expose a web server — it calls MockMvc
    // directly, so the controllers need to be on the context.
    @Autowired
    private OrdersController ordersController;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonConverter;

    @BeforeEach
    void setUpRestAssured() {
        RestAssuredMockMvc.standaloneSetup(
                MockMvcBuilders.standaloneSetup(ordersController)
                        .setMessageConverters(jacksonConverter));
    }

    // Placeholder — replace with the actual controller(s) your contracts
    // exercise. Contract verification is a producer-side concern: you
    // own the base class, you own the controllers it wires.
    static class OrdersController {
    }
}
