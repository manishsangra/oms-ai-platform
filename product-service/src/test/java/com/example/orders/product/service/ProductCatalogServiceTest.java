package com.example.orders.product.service;

import com.example.orders.product.dto.ProductRequest;
import com.example.orders.product.dto.ProductResponse;
import com.example.orders.product.entity.Product;
import com.example.orders.product.exception.ProductNotFoundException;
import com.example.orders.product.repository.ProductRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCatalogServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductCatalogService productCatalogService;

    @Test
    void shouldCreateProductSuccessfully() {

        // Arrange
        ProductRequest request = new ProductRequest(
                "Laptop",
                "Dell Laptop",
                new BigDecimal("75000.00"),
                "INR"
        );

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response =
                productCatalogService.createProduct(request);

        // Assert
        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.description()).isEqualTo("Dell Laptop");
        assertThat(response.price())
                .isEqualByComparingTo("75000.00");
        assertThat(response.currency()).isEqualTo("INR");

        verify(productRepository, times(1))
                .save(any(Product.class));
    }

    @Test
    void shouldReturnAllProducts() {

        // Arrange
        Product product1 = new Product(
                "Laptop", "Dell Laptop",
                new BigDecimal("75000"), "INR"
        );

        Product product2 = new Product(
                "Mobile", "Samsung Mobile",
                new BigDecimal("30000"), "INR"
        );

        when(productRepository.findAll())
                .thenReturn(List.of(product1, product2));

        // Act
        List<ProductResponse> responses =
                productCatalogService.getAllProducts();

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(ProductResponse::name)
                .containsExactly("Laptop", "Mobile");

        verify(productRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnProductById() {

        // Arrange
        Long productId = 1L;

        Product product = new Product(
                "Laptop",
                "Dell Laptop",
                new BigDecimal("75000"),
                "INR"
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        // Act
        ProductResponse response =
                productCatalogService.getProductById(productId);

        // Assert
        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.price())
                .isEqualByComparingTo("75000");

        verify(productRepository).findById(productId);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        // Arrange
        Long productId = 999L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> productCatalogService.getProductById(productId)
        );

        verify(productRepository).findById(productId);
    }

    @Test
    void shouldUpdateProductSuccessfully() {

        // Arrange
        Long productId = 1L;

        Product existingProduct = new Product(
                "Laptop",
                "Old Description",
                new BigDecimal("70000"),
                "INR"
        );

        ProductRequest updateRequest = new ProductRequest(
                "Updated Laptop",
                "Updated Description",
                new BigDecimal("80000"),
                "INR"
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response =
                productCatalogService.updateProduct(
                        productId, updateRequest
                );

        // Assert
        assertThat(response.name()).isEqualTo("Updated Laptop");
        assertThat(response.description())
                .isEqualTo("Updated Description");
        assertThat(response.price())
                .isEqualByComparingTo("80000");

        verify(productRepository).findById(productId);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void shouldDeleteProductSuccessfully() {

        // Arrange
        Long productId = 1L;

        Product product = new Product(
                "Laptop",
                "Dell Laptop",
                new BigDecimal("75000"),
                "INR"
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        // Act
        productCatalogService.deleteProduct(productId);

        // Assert
        verify(productRepository).findById(productId);
        verify(productRepository).delete(product);
    }
}
