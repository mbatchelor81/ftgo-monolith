package com.ftgo.domain;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

/** Spring Data repository for {@link Order} entities. */
public interface OrderRepository extends CrudRepository<Order, Long> {
    List<Order> findAllByConsumerId(Long consumerId);
}
