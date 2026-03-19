package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.common.CommonConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Configuration
@EnableAutoConfiguration
@EntityScan
@EnableJpaRepositories
@Import(CommonConfiguration.class)
public class OrderConfiguration {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public RestaurantServiceClient restaurantServiceClient(RestTemplate restTemplate,
                                                          @Value("${restaurant.service.url}") String restaurantServiceUrl) {
    return new RestaurantServiceClient(restTemplate, restaurantServiceUrl);
  }

  @Bean
  public ConsumerServiceClient consumerServiceClient(RestTemplate restTemplate,
                                                      @Value("${consumer.service.url}") String consumerServiceUrl) {
    return new ConsumerServiceClient(restTemplate, consumerServiceUrl);
  }

  @Bean
  public DeliveryServiceClient deliveryServiceClient(RestTemplate restTemplate,
                                                      @Value("${delivery.service.url}") String deliveryServiceUrl) {
    return new DeliveryServiceClient(restTemplate, deliveryServiceUrl);
  }

  @Bean
  public OrderService orderService(OrderRepository orderRepository,
                                   RestaurantServiceClient restaurantServiceClient,
                                   Optional<MeterRegistry> meterRegistry,
                                   ConsumerServiceClient consumerServiceClient,
                                   DeliveryServiceClient deliveryServiceClient) {
    return new OrderService(orderRepository,
            restaurantServiceClient,
            meterRegistry,
            consumerServiceClient, deliveryServiceClient);
  }

  @Bean
  public MeterRegistryCustomizer meterRegistryCustomizer(@Value("${spring.application.name}") String serviceName) {
    return registry -> registry.config().commonTags("service", serviceName);
  }
}
