package com.ftgo.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        // Disable Redis for context-load tests
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=0",
        "spring.cloud.gateway.redis-rate-limiter.enabled=false",
        // Use an unreachable URI so no real connections are attempted
        "FTGO_ORDER_SERVICE_URL=http://localhost:0",
        "FTGO_CONSUMER_SERVICE_URL=http://localhost:0",
        "FTGO_RESTAURANT_SERVICE_URL=http://localhost:0",
        "FTGO_COURIER_SERVICE_URL=http://localhost:0"
})
class ApiGatewayApplicationTest {

    @Test
    void contextLoads() {
        // Verifies the Spring application context starts successfully
    }
}
