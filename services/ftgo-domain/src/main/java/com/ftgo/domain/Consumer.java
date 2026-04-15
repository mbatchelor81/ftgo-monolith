package com.ftgo.domain;

import com.ftgo.common.Money;
import com.ftgo.common.PersonName;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

/** JPA entity representing a consumer who places orders. */
@Entity
@Table(name = "consumers")
@Access(AccessType.FIELD)
@DynamicUpdate
public class Consumer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded private PersonName name;

    private Consumer() {}

    public Consumer(PersonName name) {
        this.name = name;
    }

    public void validateOrderByConsumer(Money orderTotal) {
        // implement some business logic
    }

    public Long getId() {
        return id;
    }

    public PersonName getName() {
        return name;
    }
}
