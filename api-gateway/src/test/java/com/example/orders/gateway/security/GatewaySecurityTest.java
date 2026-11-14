
package com.example.orders.gateway.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewaySecurityTest {

    @Autowired
    private WebTestClient webTestClient;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String generateToken(String secret, List<String> roles,
                                 Instant expiry) {

        SecretKey secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        JwtEncoder encoder = new NimbusJwtEncoder(
                new ImmutableSecret<>(secretKey)
        );

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("gateway-test-user")
                .issuedAt(expiry.isBefore(now)
                        ? expiry.minusSeconds(300)
                        : now.minusSeconds(10))
                .expiresAt(expiry)
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return encoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    @Test
    void requestWithoutTokenShouldReturn401() {

        webTestClient.get()
                .uri("/api/v1/products")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void validUserTokenShouldPassGatewaySecurity() {

        String token = generateToken(
                jwtSecret,
                List.of("ROLE_USER"),
                Instant.now().plusSeconds(300)
        );

        // No downstream route is configured in this test.
        // 404 means the request passed Gateway security.
        webTestClient.get()
                .uri("/api/v1/products")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void userCannotCreateProduct() {

        String token = generateToken(
                jwtSecret,
                List.of("ROLE_USER"),
                Instant.now().plusSeconds(300)
        );

        webTestClient.post()
                .uri("/api/v1/products")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void expiredTokenShouldReturn401() {

        String token = generateToken(
                jwtSecret,
                List.of("ROLE_ADMIN"),
                Instant.now().minusSeconds(300)
        );

        webTestClient.get()
                .uri("/api/v1/products")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void tokenSignedWithWrongSecretShouldReturn401() {

        String token = generateToken(
                "this-is-a-different-secret-key-for-testing",
                List.of("ROLE_ADMIN"),
                Instant.now().plusSeconds(300)
        );

        webTestClient.get()
                .uri("/api/v1/products")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void actuatorHealthShouldBeAccessibleWithoutToken() {

        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }
}
