
package com.example.orders.order.messaging;

import com.example.orders.order.event.OrderCreatedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaProducerService.class);

    private static final String TOPIC = "order-created";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public KafkaProducerService(
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {

        String messageKey = event.orderId().toString();

        kafkaTemplate.send(TOPIC, messageKey, event)
                .whenComplete((result, exception) -> {

                    if (exception != null) {
                        log.error(
                                "Failed to publish OrderCreatedEvent for order ID: {}",
                                event.orderId(),
                                exception
                        );
                    } else {
                        SendResult<String, OrderCreatedEvent> sendResult = result;

                        log.info(
                                "OrderCreatedEvent published successfully. Order ID: {}, Topic: {}, Partition: {}",
                                event.orderId(),
                                sendResult.getRecordMetadata().topic(),
                                sendResult.getRecordMetadata().partition()
                        );
                    }
                });
    }
}
