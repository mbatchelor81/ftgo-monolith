package com.ftgo.domain;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Long> {
    List<Order> findAllByConsumerId(Long consumerId);
}
