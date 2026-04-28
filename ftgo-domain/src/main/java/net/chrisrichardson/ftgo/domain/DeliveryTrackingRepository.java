package net.chrisrichardson.ftgo.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryTrackingRepository extends CrudRepository<DeliveryTracking, Long> {

  Optional<DeliveryTracking> findByOrderId(Long orderId);

  List<DeliveryTracking> findByCourierIdAndStatus(Long courierId, DeliveryStatus status);
}
