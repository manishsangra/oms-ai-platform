
package com.example.orders.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ProductServiceApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void productLifecycleIntegrationTest() throws Exception {

        // 1. Create product
        String requestBody = """
                {
                    "name": "Integration Test Laptop",
                    "description": "Laptop created during integration testing",
                    "price": 75000.00,
                    "currency": "INR"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Integration Test Laptop"))
                .andExpect(jsonPath("$.price").value(75000.00))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andReturn();

        JsonNode createdProduct = objectMapper.readTree(
                createResult.getResponse().getContentAsString());

        Long productId = createdProduct.get("id").asLong();

        // 2. Retrieve product
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Integration Test Laptop"));

        // 3. Update product
        String updateRequest = """
                {
                    "name": "Updated Integration Laptop",
                    "description": "Updated product description",
                    "price": 80000.00,
                    "currency": "INR"
                }
                """;

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Integration Laptop"))
                .andExpect(jsonPath("$.price").value(80000.00));

        // 4. Verify updated product
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Integration Laptop"))
                .andExpect(jsonPath("$.price").value(80000.00));

        // 5. Delete product
        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isNoContent());

        // 6. Verify deleted product returns 404
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isNotFound());
    }
}
