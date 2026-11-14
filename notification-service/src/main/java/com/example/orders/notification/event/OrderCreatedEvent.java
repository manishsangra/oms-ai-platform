
package com.example.orders.notification.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        String eventType,
        Long orderId,
        Long customerId,
        BigDecimal totalAmount,
        String currency,
        String status,
        String createdAt
) {
}
