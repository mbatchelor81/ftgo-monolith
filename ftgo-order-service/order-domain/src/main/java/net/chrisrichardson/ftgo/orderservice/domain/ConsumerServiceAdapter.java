package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import org.springframework.stereotype.Component;

@Component
public class ConsumerServiceAdapter implements ConsumerServiceInterface {
    private final ConsumerService consumerService;
    
    public ConsumerServiceAdapter(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }
    
    @Override
    public void validateOrderForConsumer(long consumerId, Money orderTotal) {
        consumerService.validateOrderForConsumer(consumerId, orderTotal);
    }
}
