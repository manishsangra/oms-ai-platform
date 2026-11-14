package com.example.orders.order.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order not found with ID: " + orderId);
    }
}