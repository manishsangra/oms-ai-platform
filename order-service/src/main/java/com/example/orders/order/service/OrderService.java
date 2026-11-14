package com.example.orders.order.service;

import com.example.orders.order.client.ProductClient;
import com.example.orders.order.dto.*;
import com.example.orders.order.entity.*;
import com.example.orders.order.exception.OrderNotFoundException;
import com.example.orders.order.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.example.orders.order.event.OrderCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final RestClient restClient;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(
            OrderRepository orderRepository,
            ProductClient productClient,
            RestClient.Builder builder,
            ApplicationEventPublisher eventPublisher) {

        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.restClient = builder
                .baseUrl("http://PRODUCT-SERVICE")
                .build();
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest request, String authorizationToken) {

        Order order = new Order(
                request.customerId(), "INR"
        );

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {

            // Fetch product details from Product Service
            ProductResponse product =
                    productClient.getProductById(
                            itemRequest.productId(), authorizationToken
                    );

            // Ensure all items use the same currency
            if (!"INR".equals(product.currency())) {
                throw new IllegalArgumentException(
                        "Only INR products are supported currently"
                );
            }

            // Calculate individual item total
            OrderItem orderItem = new OrderItem(
                    product.id(),
                    product.name(),
                    itemRequest.quantity(),
                    product.price()
            );

            order.addItem(orderItem);

            totalAmount = totalAmount.add(
                    orderItem.getLineTotal()
            );
        }

        order.setTotalAmount(totalAmount);

        // Save order and all its items in one transaction
        // Save order and all its items in one transaction
        Order savedOrder = orderRepository.save(order);

// Create OrderCreatedEvent
        OrderCreatedEvent event = new OrderCreatedEvent(
                "ORDER_CREATED",
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getTotalAmount(),
                savedOrder.getCurrency(),
                savedOrder.getStatus().name(),
                savedOrder.getCreatedAt().toString()
        );

// Publish Spring application event
        eventPublisher.publishEvent(event);

        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));


        return mapToResponse(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> itemResponses =
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getProductId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getLineTotal()
                        ))
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getStatus(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}