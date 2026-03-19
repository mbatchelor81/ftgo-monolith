package net.chrisrichardson.ftgo.deliveryservice.domain;

import net.chrisrichardson.ftgo.common.CommonConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAutoConfiguration
@EntityScan
@EnableJpaRepositories
@Import(CommonConfiguration.class)
public class DeliveryServiceConfiguration {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public DeliveryService deliveryService(DeliveryRepository deliveryRepository,
                                         RestTemplate restTemplate,
                                         @Value("${courier.service.url:http://localhost:8083}") String courierServiceUrl) {
    return new DeliveryService(deliveryRepository, restTemplate, courierServiceUrl);
  }
}
