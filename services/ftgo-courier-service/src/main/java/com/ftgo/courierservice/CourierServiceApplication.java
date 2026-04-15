package com.ftgo.courierservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Spring Boot entry point for the Courier Service. */
@SpringBootApplication
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class CourierServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourierServiceApplication.class, args);
    }
}
