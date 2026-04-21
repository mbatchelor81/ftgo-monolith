package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.common.errors.GlobalExceptionHandler;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Explicitly imports the shared {@link GlobalExceptionHandler} so the
 * consumer service routes every thrown exception through the same
 * {@code @RestControllerAdvice} used by the other services. See
 * {@link net.chrisrichardson.ftgo.orderservice.web.OrderWebConfiguration}
 * for the rationale.
 */
@Configuration
@ComponentScan
@Import({ConsumerConfiguration.class, GlobalExceptionHandler.class})
public class ConsumerWebConfiguration {
}
