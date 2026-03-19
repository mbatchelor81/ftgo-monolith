package net.chrisrichardson.ftgo.orderservice.domain;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

public class DeliveryServiceClient {

  private RestTemplate restTemplate;
  private String deliveryServiceUrl;

  public DeliveryServiceClient(RestTemplate restTemplate, String deliveryServiceUrl) {
    this.restTemplate = restTemplate;
    this.deliveryServiceUrl = deliveryServiceUrl;
  }

  public DeliveryDTO scheduleDelivery(Long orderId, LocalDateTime readyBy) {
    ScheduleDeliveryRequest request = new ScheduleDeliveryRequest(orderId, readyBy);
    return restTemplate.postForObject(
            deliveryServiceUrl + "/deliveries",
            request,
            DeliveryDTO.class);
  }

  public List<DeliveryDTO> findByOrderId(Long orderId) {
    return restTemplate.exchange(
            deliveryServiceUrl + "/deliveries?orderId=" + orderId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<DeliveryDTO>>() {}).getBody();
  }
}
