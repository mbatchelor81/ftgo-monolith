package com.ftgo.courier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the standalone Courier microservice.
 *
 * The domain, web, and config packages under {@code com.ftgo.courier} are
 * populated as code is extracted from the legacy {@code ftgo-courier-service}
 * module during the EM-3x migration work.
 */
@SpringBootApplication
public class CourierServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourierServiceApplication.class, args);
    }
}
