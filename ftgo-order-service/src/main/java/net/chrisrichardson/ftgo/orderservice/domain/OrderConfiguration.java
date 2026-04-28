package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.DeliveryTrackingRepository;
import net.chrisrichardson.ftgo.domain.DomainConfiguration;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Optional;

@Configuration
@Import(DomainConfiguration.class)
public class OrderConfiguration {

  @Bean
  public OrderService orderService(RestaurantRepository restaurantRepository,
                                   OrderRepository orderRepository,
                                   Optional<MeterRegistry> meterRegistry,
                                   ConsumerService consumerService,
                                   CourierRepository courierRepository,
                                   CourierService courierService,
                                   DeliveryTrackingService deliveryTrackingService) {
    return new OrderService(orderRepository,
            restaurantRepository,
            meterRegistry,
            consumerService, courierRepository,
            courierService, deliveryTrackingService);
  }

  @Bean
  public DeliveryTrackingService deliveryTrackingService(DeliveryTrackingRepository deliveryTrackingRepository) {
    return new DeliveryTrackingService(deliveryTrackingRepository);
  }

  @Bean
  public MeterRegistryCustomizer meterRegistryCustomizer(@Value("${spring.application.name}") String serviceName) {
    return registry -> registry.config().commonTags("service", serviceName);
  }
}
