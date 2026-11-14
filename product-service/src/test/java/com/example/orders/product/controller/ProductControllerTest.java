package com.example.orders.product.controller;

import com.example.orders.product.dto.ProductRequest;
import com.example.orders.product.dto.ProductResponse;
import com.example.orders.product.exception.ProductNotFoundException;
import com.example.orders.product.service.ProductCatalogService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductCatalogService productService;

    @Test
    void shouldReturnAllProducts() throws Exception {

        // Arrange
        ProductResponse product = new ProductResponse(
                1L, "Laptop", "Dell Laptop",
                new BigDecimal("75000"), "INR"
        );

        when(productService.getAllProducts())
                .thenReturn(List.of(product));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].currency").value("INR"));

        verify(productService).getAllProducts();
    }

    @Test
    void shouldReturnProductById() throws Exception {

        // Arrange
        ProductResponse product = new ProductResponse(
                1L, "Laptop", "Dell Laptop",
                new BigDecimal("75000"), "INR"
        );

        when(productService.getProductById(1L))
                .thenReturn(product);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.description").value("Dell Laptop"));

        verify(productService).getProductById(1L);
    }

    @Test
    void shouldCreateProductSuccessfully() throws Exception {

        // Arrange
        ProductResponse response = new ProductResponse(
                1L, "Laptop", "Dell Laptop",
                new BigDecimal("75000"), "INR"
        );

        when(productService.createProduct(any(ProductRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                    "name": "Laptop",
                    "description": "Dell Laptop",
                    "price": 75000,
                    "currency": "INR"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.currency").value("INR"));

        verify(productService).createProduct(any(ProductRequest.class));
    }

    @Test
    void shouldUpdateProductSuccessfully() throws Exception {

        // Arrange
        ProductResponse response = new ProductResponse(
                1L, "Updated Laptop", "Updated Description",
                new BigDecimal("80000"), "INR"
        );

        when(productService.updateProduct(
                eq(1L), any(ProductRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                    "name": "Updated Laptop",
                    "description": "Updated Description",
                    "price": 80000,
                    "currency": "INR"
                }
                """;

        // Act & Assert
        mockMvc.perform(put("/api/v1/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop"))
                .andExpect(jsonPath("$.price").value(80000));

        verify(productService).updateProduct(
                eq(1L), any(ProductRequest.class));
    }

    @Test
    void shouldDeleteProductSuccessfully() throws Exception {

        // Arrange
        doNothing().when(productService).deleteProduct(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(productService).deleteProduct(1L);
    }

    @Test
    void shouldReturn404WhenProductDoesNotExist() throws Exception {

        // Arrange
        when(productService.getProductById(999L))
                .thenThrow(new ProductNotFoundException(999L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());

        verify(productService).getProductById(999L);
    }

    @Test
    void shouldReturn400WhenCreatingProductWithInvalidData()
            throws Exception {

        // Arrange
        String invalidRequest = """
                {
                    "name": "",
                    "description": "Invalid product",
                    "price": 0,
                    "currency": "inr"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        containsString("name")))
                .andExpect(jsonPath("$.error").value(
                        containsString("price")))
                .andExpect(jsonPath("$.error").value(
                        containsString("currency")));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldReturn400WhenUpdatingProductWithInvalidData()
            throws Exception {

        // Arrange
        String invalidRequest = """
                {
                    "name": "",
                    "description": "Invalid product",
                    "price": -10,
                    "currency": "inr"
                }
                """;

        // Act & Assert
        mockMvc.perform(put("/api/v1/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(productService);
    }
}
