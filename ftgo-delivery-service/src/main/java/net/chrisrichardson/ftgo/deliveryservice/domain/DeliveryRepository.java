package net.chrisrichardson.ftgo.deliveryservice.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DeliveryRepository extends CrudRepository<Delivery, Long> {
  List<Delivery> findByOrderId(Long orderId);
}
