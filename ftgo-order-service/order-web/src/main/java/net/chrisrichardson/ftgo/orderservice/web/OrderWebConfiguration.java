package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.orderservice.data.OrderDataConfiguration;
import net.chrisrichardson.ftgo.orderservice.domain.OrderConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan
@Import({OrderConfiguration.class, OrderDataConfiguration.class})
public class OrderWebConfiguration {
}
