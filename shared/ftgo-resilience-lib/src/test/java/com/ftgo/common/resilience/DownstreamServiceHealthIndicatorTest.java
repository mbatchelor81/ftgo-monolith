package com.ftgo.common.resilience;

import com.ftgo.common.resilience.health.DownstreamServiceHealthIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DownstreamServiceHealthIndicatorTest {

    @Test
    void health_whenServiceIsUp_returnsUp() {
        // Arrange
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"status\":\"UP\"}"));

        DownstreamServiceHealthIndicator indicator =
                new DownstreamServiceHealthIndicator("order-service", "http://order-service:8080/actuator/health", restTemplate);

        // Act
        Health health = indicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("service", "order-service");
    }

    @Test
    void health_whenServiceIsDown_returnsDown() {
        // Arrange
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        DownstreamServiceHealthIndicator indicator =
                new DownstreamServiceHealthIndicator("order-service", "http://order-service:8080/actuator/health", restTemplate);

        // Act
        Health health = indicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("service", "order-service");
        assertThat(health.getDetails()).containsKey("error");
    }
}
