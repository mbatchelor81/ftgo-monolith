package com.ftgo.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the standalone Order microservice.
 *
 * The domain, web, and config packages under {@code com.ftgo.order} are
 * populated as code is extracted from the legacy {@code ftgo-order-service}
 * module during the EM-3x migration work.
 */
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
