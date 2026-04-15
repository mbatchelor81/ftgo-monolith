package com.ftgo.domain;

import com.ftgo.common.Address;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Embeddable
@Access(AccessType.FIELD)
public class DeliveryInformation {

    private LocalDateTime deliveryTime;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street1", column = @Column(name = "delivery_address_street1")),
            @AttributeOverride(name = "street2", column = @Column(name = "delivery_address_street2")),
            @AttributeOverride(name = "city", column = @Column(name = "delivery_address_city")),
            @AttributeOverride(name = "state", column = @Column(name = "delivery_address_state")),
            @AttributeOverride(name = "zip", column = @Column(name = "delivery_address_zip")),
    })
    private Address deliveryAddress;

    public DeliveryInformation() {
    }

    public DeliveryInformation(LocalDateTime deliveryTime, Address deliveryAddress) {
        this.deliveryTime = deliveryTime;
        this.deliveryAddress = deliveryAddress;
    }

    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(LocalDateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(Address deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
}
