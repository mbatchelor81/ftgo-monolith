package com.ftgo.domain;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CourierRepository extends CrudRepository<Courier, Long> {

    @Query("SELECT c FROM Courier c WHERE c.available = true")
    List<Courier> findAllAvailable();
}
