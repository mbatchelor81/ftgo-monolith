package com.ftgo.BOUNDED_CONTEXT.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

// =============================================================================
// CONTRACT TEST TEMPLATE
// =============================================================================
// Copy this template and replace:
//   - BOUNDED_CONTEXT → the consuming service context
//   - ProducerServiceApi → the API interface of the producer service
//
// Contract tests verify that the API contract between services is upheld.
// The consumer side defines the expected interactions; the producer side
// verifies its implementation against those expectations.
//
// Approach Options:
//   1. Spring Cloud Contract — Gradle plugin, auto-generated stubs
//   2. Pact — Language-agnostic, requires Pact Broker
//
// This template shows a lightweight consumer-driven contract test using
// Spring Boot Test and TestRestTemplate.
//
// Conventions:
//   - File location: src/contractTest/java/com/ftgo/<context>/contract/<Name>ContractTest.java
//   - Each test verifies one API interaction (request + response)
//   - Assertions cover: status code, response body structure, content type
// =============================================================================

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProducerApiContractTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getEntityById_shouldReturnEntityWithRequiredFields() {
        // This test verifies that the producer's API returns the expected
        // response structure when called with a valid entity ID.

        // Act
        // ResponseEntity<String> response = restTemplate.getForEntity(
        //     "/api/entities/{id}", String.class, 1L);

        // Assert — verify the contract
        // assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // assertThat(response.getHeaders().getContentType().toString())
        //     .contains("application/json");

        // Verify required fields exist in the response body
        // String body = response.getBody();
        // assertThat(body).contains("\"id\"");
        // assertThat(body).contains("\"state\"");
        // assertThat(body).contains("\"createdAt\"");
    }

    @Test
    void getEntityById_notFound_shouldReturn404() {
        // Act
        // ResponseEntity<String> response = restTemplate.getForEntity(
        //     "/api/entities/{id}", String.class, 999L);

        // Assert
        // assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createEntity_shouldAcceptValidPayload() {
        // This test verifies that the producer accepts the request format
        // defined by the consumer's API client.

        // Arrange
        // String requestBody = """
        //     {
        //         "name": "Test Entity",
        //         "type": "STANDARD"
        //     }
        //     """;
        // HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_JSON);
        // HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        // Act
        // ResponseEntity<String> response = restTemplate.postForEntity(
        //     "/api/entities", request, String.class);

        // Assert — verify the contract
        // assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
