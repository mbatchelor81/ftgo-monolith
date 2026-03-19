package net.chrisrichardson.ftgo.deliveryservice.domain;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Transactional
public class DeliveryService {

  private DeliveryRepository deliveryRepository;
  private RestTemplate restTemplate;
  private String courierServiceUrl;
  private Random random = new Random();

  public DeliveryService(DeliveryRepository deliveryRepository, RestTemplate restTemplate, String courierServiceUrl) {
    this.deliveryRepository = deliveryRepository;
    this.restTemplate = restTemplate;
    this.courierServiceUrl = courierServiceUrl;
  }

  public Delivery scheduleDelivery(Long orderId, LocalDateTime readyBy) {
    CourierDTO[] couriers = restTemplate.getForObject(
            courierServiceUrl + "/couriers/available", CourierDTO[].class);

    if (couriers == null || couriers.length == 0) {
      throw new NoCouriersAvailableException();
    }

    CourierDTO courier = couriers[random.nextInt(couriers.length)];

    LocalDateTime pickupTime = readyBy;
    LocalDateTime dropoffTime = readyBy.plusMinutes(30);

    Delivery delivery = new Delivery(orderId, courier.getId(), pickupTime, dropoffTime);
    delivery.addAction(DeliveryAction.makePickup(orderId));
    delivery.addAction(DeliveryAction.makeDropoff(orderId, dropoffTime));

    deliveryRepository.save(delivery);

    return delivery;
  }

  public List<Delivery> findByOrderId(Long orderId) {
    return deliveryRepository.findByOrderId(orderId);
  }
}
