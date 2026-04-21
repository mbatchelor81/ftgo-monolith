package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.common.errors.GlobalExceptionHandler;
import net.chrisrichardson.ftgo.courierservice.domain.CourierServiceConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Explicitly imports the shared {@link GlobalExceptionHandler} so the
 * courier service returns the canonical FTGO error payload for every
 * uncaught exception.
 */
@Configuration
@Import({CourierServiceConfiguration.class, GlobalExceptionHandler.class})
@ComponentScan
public class CourierWebConfiguration {
}
