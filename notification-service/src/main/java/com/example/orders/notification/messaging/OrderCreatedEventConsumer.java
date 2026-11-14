
package com.example.orders.notification.messaging;

import com.example.orders.notification.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(OrderCreatedEventConsumer.class);

    @KafkaListener(
            topics = "order-created",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(OrderCreatedEvent event) {

        log.info("========================================");
        log.info("NOTIFICATION SERVICE: Order Created Event Received!");
        log.info("Event Type: {}", event.eventType());
        log.info("Order ID: {}", event.orderId());
        log.info("Customer ID: {}", event.customerId());
        log.info("Total Amount: {} {}", event.totalAmount(), event.currency());
        log.info("Order Status: {}", event.status());
        log.info("Created At: {}", event.createdAt());
        log.info("========================================");
    }
}
