package com.ftgo.domain;

import com.ftgo.common.Address;
import com.ftgo.common.PersonName;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.List;
import org.hibernate.annotations.DynamicUpdate;

/** JPA entity representing a delivery courier. */
@Entity
@Access(AccessType.FIELD)
@DynamicUpdate
public class Courier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded private PersonName name;

    @Embedded private Address address;

    @Embedded private Plan plan;

    private Boolean available;

    public Courier() {}

    public Courier(PersonName name, Address address) {
        this.name = name;
        this.address = address;
    }

    public void noteAvailable() {
        this.available = true;
    }

    public void addAction(Action action) {
        plan.add(action);
    }

    public void cancelDelivery(Order order) {
        plan.removeDelivery(order);
    }

    public boolean isAvailable() {
        return available;
    }

    public Plan getPlan() {
        return plan;
    }

    public Long getId() {
        return id;
    }

    public void noteUnavailable() {
        this.available = false;
    }

    public List<Action> actionsForDelivery(Order order) {
        return plan.actionsForDelivery(order);
    }
}
