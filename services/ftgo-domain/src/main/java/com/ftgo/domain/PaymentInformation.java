package com.ftgo.domain;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Embeddable;

@Embeddable
@Access(AccessType.FIELD)
public class PaymentInformation {

    private String paymentToken;

    public PaymentInformation() {}

    public PaymentInformation(String paymentToken) {
        this.paymentToken = paymentToken;
    }

    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }
}
