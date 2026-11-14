
package com.example.orders.product.security;

import com.example.orders.product.ProductServiceApplication;
import com.example.orders.product.service.ProductCatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ProductServiceApplication.class)
@AutoConfigureMockMvc
class ProductSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductCatalogService productCatalogService;

    @Test
    void getProductsWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProductsAsUserShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .with(jwt().authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getProductsAsAdminShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .with(jwt().authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void createProductAsUserShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .with(jwt().authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProductAsUserShouldReturn403() throws Exception {
        mockMvc.perform(put("/api/v1/products/1")
                        .with(jwt().authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteProductAsUserShouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/v1/products/1")
                        .with(jwt().authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void actuatorHealthShouldBeAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
