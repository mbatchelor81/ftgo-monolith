package com.ftgo.consumerservice.api.web;

import com.ftgo.common.PersonName;

public class CreateConsumerRequest {

    private PersonName name;

    private CreateConsumerRequest() {}

    public CreateConsumerRequest(PersonName name) {
        this.name = name;
    }

    public PersonName getName() {
        return name;
    }

    public void setName(PersonName name) {
        this.name = name;
    }
}
