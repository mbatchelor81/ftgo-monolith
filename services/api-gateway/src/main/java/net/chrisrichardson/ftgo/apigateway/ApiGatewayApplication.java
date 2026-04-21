package net.chrisrichardson.ftgo.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the FTGO API Gateway.
 *
 * <p>The gateway is the single ingress for the platform: it validates JWTs,
 * applies Redis-backed rate limiting, wraps downstream calls with Resilience4j
 * circuit breakers, and routes requests to the appropriate microservice.
 *
 * <p>Route definitions, security, and resilience defaults live in
 * {@code application.yml} alongside programmatic beans in the
 * {@link net.chrisrichardson.ftgo.apigateway.config} package.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
