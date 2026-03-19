package net.chrisrichardson.ftgo.deliveryservice;

import net.chrisrichardson.ftgo.deliveryservice.web.DeliveryWebConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(DeliveryWebConfiguration.class)
public class DeliveryServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(DeliveryServiceApplication.class, args);
  }
}
