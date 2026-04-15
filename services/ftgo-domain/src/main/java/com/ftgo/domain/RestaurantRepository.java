package com.ftgo.domain;

import org.springframework.data.repository.CrudRepository;

/** Spring Data repository for {@link Restaurant} entities. */
public interface RestaurantRepository extends CrudRepository<Restaurant, Long> {}
