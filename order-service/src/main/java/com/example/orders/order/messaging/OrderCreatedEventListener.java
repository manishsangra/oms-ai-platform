
package com.example.orders.order.messaging;

import com.example.orders.order.event.OrderCreatedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderCreatedEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(OrderCreatedEventListener.class);

    private final KafkaProducerService kafkaProducerService;

    public OrderCreatedEventListener(
            KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleOrderCreated(OrderCreatedEvent event) {

        log.info(
                "Database transaction committed. Publishing order event for ID: {}",
                event.orderId()
        );

        kafkaProducerService.publishOrderCreated(event);
    }
}
