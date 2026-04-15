package com.ftgo.apigateway.circuitbreaker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Unit tests for {@link FallbackController}. */
class FallbackControllerTest {

    private final FallbackController controller = new FallbackController();

    @Test
    void fallback_returns503WithStructuredBody() {
        ResponseEntity<Map<String, Object>> response = controller.fallback();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("status", 503);
        assertThat(response.getBody()).containsEntry("error", "Service Unavailable");
        assertThat(response.getBody()).containsKey("message");
    }
}
