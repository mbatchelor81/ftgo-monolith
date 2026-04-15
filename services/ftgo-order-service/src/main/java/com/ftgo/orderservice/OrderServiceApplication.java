package com.ftgo.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Spring Boot entry point for the Order Service. */
@SpringBootApplication
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
