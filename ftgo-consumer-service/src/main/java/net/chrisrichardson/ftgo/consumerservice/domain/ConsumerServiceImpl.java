package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerService;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public class ConsumerServiceImpl implements ConsumerService {

  @Autowired
  private ConsumerRepository consumerRepository;

  @Override
  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    Optional<Consumer> consumer = consumerRepository.findById(consumerId);
    consumer.orElseThrow(ConsumerNotFoundException::new).validateOrderByConsumer(orderTotal);
  }

  @Override
  public long create(PersonName name) {
    Consumer consumer = consumerRepository.save(new Consumer(name));
    return consumer.getId();
  }

  @Override
  public boolean existsById(long consumerId) {
    return consumerRepository.existsById(consumerId);
  }

  public Consumer createConsumer(PersonName name) {
    Consumer consumer = consumerRepository.save(new Consumer(name));
    return consumer;
  }

  public Optional<Consumer> findById(long consumerId) {
    return consumerRepository.findById(consumerId);
  }
}
