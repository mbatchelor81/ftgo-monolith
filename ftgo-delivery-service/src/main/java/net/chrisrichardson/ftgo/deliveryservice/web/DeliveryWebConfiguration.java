package net.chrisrichardson.ftgo.deliveryservice.web;

import net.chrisrichardson.ftgo.deliveryservice.domain.DeliveryServiceConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DeliveryServiceConfiguration.class)
@ComponentScan
public class DeliveryWebConfiguration {
}
