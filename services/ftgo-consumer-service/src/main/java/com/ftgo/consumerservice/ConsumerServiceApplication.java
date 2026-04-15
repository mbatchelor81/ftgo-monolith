package com.ftgo.consumerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Spring Boot entry point for the Consumer Service. */
@SpringBootApplication
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class ConsumerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerServiceApplication.class, args);
    }
}
