package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.domain.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Transactional
public class DeliveryTrackingService {

  private DeliveryTrackingRepository deliveryTrackingRepository;

  public DeliveryTrackingService(DeliveryTrackingRepository deliveryTrackingRepository) {
    this.deliveryTrackingRepository = deliveryTrackingRepository;
  }

  public DeliveryTracking createTracking(Order order, Courier courier,
                                          double distanceKm,
                                          LocalDateTime estimatedPickupTime,
                                          LocalDateTime estimatedDeliveryTime) {
    DeliveryTracking tracking = new DeliveryTracking(
        order, courier, distanceKm, estimatedPickupTime, estimatedDeliveryTime);
    return deliveryTrackingRepository.save(tracking);
  }

  public Optional<DeliveryTracking> getTrackingByOrderId(long orderId) {
    return deliveryTrackingRepository.findByOrderId(orderId);
  }

  public void updateStatus(long orderId, DeliveryStatus newStatus) {
    deliveryTrackingRepository.findByOrderId(orderId)
        .ifPresent(tracking -> tracking.updateStatus(newStatus));
  }
}
