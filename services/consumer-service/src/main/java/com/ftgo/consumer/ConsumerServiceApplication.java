package com.ftgo.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the standalone Consumer microservice.
 *
 * The domain, web, and config packages under {@code com.ftgo.consumer} are
 * populated as code is extracted from the legacy {@code ftgo-consumer-service}
 * module during the EM-3x migration work.
 */
@SpringBootApplication
public class ConsumerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerServiceApplication.class, args);
    }
}
