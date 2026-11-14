package com.example.orders.auth.dto;

public record LoginRequest(
        String username,
        String password
) {
}
