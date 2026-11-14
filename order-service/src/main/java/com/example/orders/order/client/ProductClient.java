package com.example.orders.order.client;

import com.example.orders.order.dto.ProductResponse;
import com.example.orders.order.exception.ProductNotFoundException;
import com.example.orders.order.exception.ProductServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.apache.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(RestClient productRestClient) {
        this.restClient = productRestClient;
    }

    @CircuitBreaker(
            name = "productService",
            fallbackMethod = "productServiceFallback"
    )

    public ProductResponse getProductById(
            Long productId,
            String authorizationToken) {

        try {
            return restClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationToken)
                    .retrieve()
                    .body(ProductResponse.class);

        } catch (HttpClientErrorException.NotFound ex) {
            throw new ProductNotFoundException(productId);

        } catch (ResourceAccessException ex) {
            throw new ProductServiceUnavailableException(
                    "Product Service is unavailable. Please try again later."
            );
        }
    }

    private ProductResponse productServiceFallback(
            Long productId, Throwable ex) {

        if (ex instanceof ProductNotFoundException) {
            throw (ProductNotFoundException) ex;
        }

        throw new ProductServiceUnavailableException(
                "Product Service is temporarily unavailable. Please try again later."
        );
    }
}