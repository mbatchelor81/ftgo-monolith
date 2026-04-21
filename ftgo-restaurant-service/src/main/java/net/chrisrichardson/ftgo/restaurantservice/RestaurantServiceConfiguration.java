package net.chrisrichardson.ftgo.restaurantservice;

import net.chrisrichardson.ftgo.common.errors.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Explicitly imports the shared {@link GlobalExceptionHandler}. The handler
 * lives in {@code net.chrisrichardson.ftgo.common.errors} which is outside
 * this service's component-scan base package, so the @{@code Import} is
 * required for the @{@code RestControllerAdvice} to take effect.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
@Import(GlobalExceptionHandler.class)
public class RestaurantServiceConfiguration {
}
