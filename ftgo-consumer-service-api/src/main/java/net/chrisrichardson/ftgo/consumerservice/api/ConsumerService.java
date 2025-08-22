package net.chrisrichardson.ftgo.consumerservice.api;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;

public interface ConsumerService {
    
    void validateOrderForConsumer(long consumerId, Money orderTotal);
    
    long create(PersonName name);
    
    boolean existsById(long consumerId);
}
