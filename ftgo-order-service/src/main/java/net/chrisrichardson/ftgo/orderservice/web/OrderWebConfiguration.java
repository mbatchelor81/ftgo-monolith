package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.errors.GlobalExceptionHandler;
import net.chrisrichardson.ftgo.orderservice.domain.OrderServiceWithRepositoriesConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Explicitly imports the shared {@link GlobalExceptionHandler}. It lives in
 * {@code net.chrisrichardson.ftgo.common.errors}, which is outside this
 * service's component-scan base package, so @{@link Import} is required to
 * register the @{@code RestControllerAdvice}. Without it, ad-hoc per-
 * controller error handling would silently come back.
 */
@Configuration
@ComponentScan
@Import({OrderServiceWithRepositoriesConfiguration.class, GlobalExceptionHandler.class})
public class OrderWebConfiguration {
}
