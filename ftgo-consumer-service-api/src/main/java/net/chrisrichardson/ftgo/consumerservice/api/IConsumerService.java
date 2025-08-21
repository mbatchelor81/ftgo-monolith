package net.chrisrichardson.ftgo.consumerservice.api;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Consumer;
import java.util.Optional;

/**
 * Service interface for consumer-related operations in the FTGO application.
 * Provides methods for consumer validation, creation, and retrieval.
 */
public interface IConsumerService {

    /**
     * Validates that a consumer can place an order with the specified total amount.
     * Throws an exception if the consumer is not found or validation fails.
     *
     * @param consumerId the ID of the consumer to validate
     * @param orderTotal the total amount of the order to validate
     * @throws ConsumerNotFoundException if the consumer is not found
     */
    void validateOrderForConsumer(long consumerId, Money orderTotal);

    /**
     * Creates a new consumer with the specified name.
     *
     * @param name the name of the consumer to create
     * @return the newly created Consumer entity
     */
    Consumer create(PersonName name);

    /**
     * Finds a consumer by their ID.
     *
     * @param consumerId the ID of the consumer to find
     * @return an Optional containing the Consumer if found, empty otherwise
     */
    Optional<Consumer> findById(long consumerId);
}
