package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.springframework.web.client.RestTemplate;

public class ConsumerServiceClient {

  private RestTemplate restTemplate;
  private String consumerServiceUrl;

  public ConsumerServiceClient(RestTemplate restTemplate, String consumerServiceUrl) {
    this.restTemplate = restTemplate;
    this.consumerServiceUrl = consumerServiceUrl;
  }

  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    ValidateOrderRequest request = new ValidateOrderRequest(orderTotal);
    restTemplate.postForEntity(
            consumerServiceUrl + "/consumers/" + consumerId + "/validate",
            request,
            Void.class);
  }
}
