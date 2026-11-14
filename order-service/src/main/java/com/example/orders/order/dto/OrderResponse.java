package com.example.orders.order.dto;

import com.example.orders.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        Long customerId,
        BigDecimal totalAmount,
        String currency,
        OrderStatus status,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {}