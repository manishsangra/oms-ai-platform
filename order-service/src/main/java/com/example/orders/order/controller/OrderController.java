package com.example.orders.order.controller;

import com.example.orders.order.dto.OrderRequest;
import com.example.orders.order.dto.OrderResponse;
import com.example.orders.order.entity.Order;
import com.example.orders.order.service.OrderService;

import jakarta.validation.Valid;

import org.apache.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken) {

        return orderService.placeOrder(request, authorizationToken);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}