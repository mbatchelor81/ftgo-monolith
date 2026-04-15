package com.ftgo.SERVICENAME;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E test template — tests run against the full service stack.
 *
 * <p>E2E tests should:
 * <ul>
 *   <li>Use the @Tag("e2e") annotation so the Gradle e2eTest task picks them up</li>
 *   <li>Test critical user journeys across service boundaries</li>
 *   <li>Use REST calls to verify API contracts</li>
 *   <li>Be few in number (test pyramid: fewer E2E, more unit)</li>
 * </ul>
 *
 * <p>When copying this template to a real service, add:
 * <pre>
 * {@literal @}SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 * {@literal @}ActiveProfiles("e2e")
 * </pre>
 *
 * <p>Replace SERVICENAME with the actual service name when copying this template.
 */
@Tag("e2e")
@DisplayName("Example E2E")
class ExampleE2ETest {

    @Test
    @DisplayName("health endpoint should return UP")
    void healthEndpoint_shouldReturnUp() {
        // When copying to a real service, add @SpringBootTest and use TestRestTemplate:
        //
        // @Value("${local.server.port}")
        // private int port;
        //
        // private final TestRestTemplate restTemplate = new TestRestTemplate();
        //
        // var response = restTemplate.getForEntity(
        //     "http://localhost:" + port + "/actuator/health", String.class);
        // assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // assertThat(response.getBody()).contains("UP");
        assertThat(true).isTrue(); // Placeholder — replace with real test
    }
}
