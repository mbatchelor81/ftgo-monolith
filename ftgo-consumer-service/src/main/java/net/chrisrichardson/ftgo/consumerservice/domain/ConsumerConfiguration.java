package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.consumerservice.api.IConsumerService;
import net.chrisrichardson.ftgo.domain.DomainConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DomainConfiguration.class)
public class ConsumerConfiguration {

  @Bean
  public IConsumerService consumerService() {
    return new ConsumerService();
  }
}
