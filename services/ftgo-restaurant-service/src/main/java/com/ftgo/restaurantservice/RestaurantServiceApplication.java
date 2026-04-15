package com.ftgo.restaurantservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Spring Boot entry point for the Restaurant Service. */
@SpringBootApplication
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class RestaurantServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}
