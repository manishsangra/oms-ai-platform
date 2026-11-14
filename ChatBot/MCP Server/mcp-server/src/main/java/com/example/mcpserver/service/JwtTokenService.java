
package com.example.mcpserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Service
public class JwtTokenService {

    private final RestClient authClient;
    private final String username;
    private final String password;

    private String accessToken;
    private Instant expiresAt;

    public JwtTokenService(
            @Value("${oms.auth.url}") String authUrl,
            @Value("${oms.auth.username}") String username,
            @Value("${oms.auth.password}") String password) {

        this.authClient = RestClient.builder()
                .baseUrl(authUrl)
                .build();

        this.username = username;
        this.password = password;
    }

    public synchronized String getAccessToken() {

        if (accessToken != null
                && expiresAt != null
                && Instant.now().isBefore(expiresAt)) {
            return accessToken;
        }

        LoginResponse response = authClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(username, password))
                .retrieve()
                .body(LoginResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new IllegalStateException(
                    "Unable to obtain JWT from Auth Service");
        }

        this.accessToken = response.accessToken();

        // Refresh 60 seconds before actual expiry.
        this.expiresAt = Instant.now()
                .plusSeconds(response.expiresInSeconds() - 60);

        return accessToken;
    }

    private record LoginRequest(
            String username,
            String password) {
    }

    private record LoginResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds) {
    }
}
