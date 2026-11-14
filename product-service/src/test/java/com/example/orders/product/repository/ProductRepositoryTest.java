
package com.example.orders.product.repository;

import com.example.orders.product.entity.Product;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveProductSuccessfully() {

        // Arrange
        Product product = createProduct(
                "Laptop", "Dell Laptop", "75000", "INR"
        );

        // Act
        Product savedProduct = productRepository.saveAndFlush(product);

        // Assert
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Laptop");
        assertThat(savedProduct.getPrice())
                .isEqualByComparingTo("75000");

    }

    @Test
    void shouldFindProductById() {

        // Arrange
        Product product = createProduct(
                "Laptop", "Dell Laptop", "75000", "INR"
        );

        Product savedProduct = productRepository.saveAndFlush(product);

        // Act
        Optional<Product> result =
                productRepository.findById(savedProduct.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Laptop");
        assertThat(result.get().getCurrency()).isEqualTo("INR");

    }

    @Test
    void shouldFindAllProducts() {

        // Arrange
        Product laptop = createProduct(
                "Laptop", "Dell Laptop", "75000", "INR"
        );

        Product mobile = createProduct(
                "Mobile", "Samsung Mobile", "30000", "INR"
        );

        productRepository.save(laptop);
        productRepository.save(mobile);

        // Act
        List<Product> products = productRepository.findAll();

        // Assert
        assertThat(products).hasSize(2);
        assertThat(products)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop", "Mobile");

    }

    @Test
    void shouldUpdateProductSuccessfully() {

        // Arrange
        Product product = createProduct(
                "Laptop", "Old Description", "70000", "INR"
        );

        Product savedProduct = productRepository.saveAndFlush(product);

        // Act
        savedProduct.setName("Updated Laptop");
        savedProduct.setDescription("Updated Description");
        savedProduct.setPrice(new BigDecimal("80000"));

        productRepository.saveAndFlush(savedProduct);

        // Fetch again from database
        Product updatedProduct = productRepository
                .findById(savedProduct.getId())
                .orElseThrow();

        // Assert
        assertThat(updatedProduct.getName())
                .isEqualTo("Updated Laptop");

        assertThat(updatedProduct.getDescription())
                .isEqualTo("Updated Description");

        assertThat(updatedProduct.getPrice())
                .isEqualByComparingTo("80000");

    }

    @Test
    void shouldDeleteProductSuccessfully() {

        // Arrange
        Product product = createProduct(
                "Laptop", "Dell Laptop", "75000", "INR"
        );

        Product savedProduct = productRepository.saveAndFlush(product);

        Long productId = savedProduct.getId();

        // Act
        productRepository.delete(savedProduct);
        productRepository.flush();

        // Assert
        assertThat(productRepository.findById(productId))
                .isEmpty();

    }

    @Test
    void shouldReturnEmptyWhenProductDoesNotExist() {

        // Arrange
        Long nonExistingId = 999999L;

        // Act
        Optional<Product> result =
                productRepository.findById(nonExistingId);

        // Assert
        assertThat(result).isEmpty();

    }

    private Product createProduct(
            String name,
            String description,
            String price,
            String currency) {

        return new Product(
                name,
                description,
                new BigDecimal(price),
                currency
        );
    }
}
