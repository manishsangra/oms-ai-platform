
package com.example.mcpserver.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        Long customerId,
        BigDecimal totalAmount,
        String currency,
        String status,
        LocalDateTime createdAt,
        List<OrderItem> items
) {
    public record OrderItem(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}
}
