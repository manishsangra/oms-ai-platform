
package com.example.orders.product.security;

import com.example.orders.product.ProductServiceApplication;
import com.example.orders.product.service.ProductCatalogService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ProductServiceApplication.class)
@AutoConfigureMockMvc
class ProductRealJwtTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductCatalogService productCatalogService;

    @Value("${jwt.secret}")
    private String jwtSecret;


    private String generateToken(String secret, Instant expiry) {

        SecretKey secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        JwtEncoder encoder = new NimbusJwtEncoder(
                new ImmutableSecret<>(secretKey)
        );

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("test-admin")
                .issuedAt(expiry.isBefore(now)
                        ? expiry.minusSeconds(300)
                        : now.minusSeconds(10))
                .expiresAt(expiry)
                .claim("roles", List.of("ROLE_ADMIN"))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return encoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }


    @Test
    void validAdminTokenShouldReturn200() throws Exception {

        String token = generateToken(
                jwtSecret,
                Instant.now().plusSeconds(300)
        );

        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void expiredTokenShouldReturn401() throws Exception {

        String token = generateToken(
                jwtSecret,
                Instant.now().minusSeconds(300)
        );

        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSignedWithWrongSecretShouldReturn401() throws Exception {

        String token = generateToken(
                "this-is-a-different-secret-key-for-testing",
                Instant.now().plusSeconds(300)
        );

        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
