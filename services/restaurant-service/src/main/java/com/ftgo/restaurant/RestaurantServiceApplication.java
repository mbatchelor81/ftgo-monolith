package com.ftgo.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the standalone Restaurant microservice.
 *
 * The domain, web, and config packages under {@code com.ftgo.restaurant} are
 * populated as code is extracted from the legacy {@code ftgo-restaurant-service}
 * module during the EM-3x migration work.
 */
@SpringBootApplication
public class RestaurantServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}
