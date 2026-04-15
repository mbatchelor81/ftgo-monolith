package com.ftgo.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Spring Boot entry point for the FTGO API Gateway. */
@SpringBootApplication
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
