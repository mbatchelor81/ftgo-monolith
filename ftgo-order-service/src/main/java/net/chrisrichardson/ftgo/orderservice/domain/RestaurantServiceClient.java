package net.chrisrichardson.ftgo.orderservice.domain;

import org.springframework.web.client.RestTemplate;

public class RestaurantServiceClient {

  private RestTemplate restTemplate;
  private String restaurantServiceUrl;

  public RestaurantServiceClient(RestTemplate restTemplate, String restaurantServiceUrl) {
    this.restTemplate = restTemplate;
    this.restaurantServiceUrl = restaurantServiceUrl;
  }

  public RestaurantDTO findById(long restaurantId) {
    return restTemplate.getForObject(
            restaurantServiceUrl + "/restaurants/" + restaurantId,
            RestaurantDTO.class);
  }
}
