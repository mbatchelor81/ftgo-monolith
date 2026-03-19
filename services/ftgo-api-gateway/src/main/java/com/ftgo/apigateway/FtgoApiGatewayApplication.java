package com.ftgo.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FTGO API Gateway — single entry point for all microservices.
 *
 * <p>Routes requests to the appropriate downstream service, enforces
 * JWT authentication, rate limiting, and circuit breaking at the edge.
 */
@SpringBootApplication
public class FtgoApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FtgoApiGatewayApplication.class, args);
    }
}
