package com.example.orders.product.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01",
                message = "Price must be greater than zero")
        BigDecimal price,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "[A-Z]{3}",
                message = "Currency must be a 3-letter uppercase code")
        String currency
) {}