package net.chrisrichardson.ftgo.orderservice.data;

import net.chrisrichardson.ftgo.domain.DomainConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories
@Import(DomainConfiguration.class)
public class OrderDataConfiguration {
}
