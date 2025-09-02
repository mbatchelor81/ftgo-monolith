package net.chrisrichardson.ftgo.orderservice.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CourierRepository extends CrudRepository<Courier, Long> {

  List<Courier> findAllAvailable();

}
