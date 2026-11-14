
package com.example.mcpserver.service;

import com.example.mcpserver.dto.OrderResponse;
import com.example.mcpserver.tools.OrderTools;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Duration;

@Service
public class OrderCacheService {

    private static final Logger log =
            LoggerFactory.getLogger(OrderTools.class);

    private static final Duration CACHE_TTL =
            Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final JwtTokenService jwtTokenService;
    private final RestClient orderClient;

    public OrderCacheService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            JwtTokenService jwtTokenService,
            @Value("${oms.api-gateway.url}") String gatewayUrl) {

        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.jwtTokenService = jwtTokenService;

        this.orderClient = RestClient.builder()
                .baseUrl(gatewayUrl)
                .build();
    }

    public OrderResponse getOrder(Long orderId) {

        String cacheKey = "oms:order:" + orderId;

        // 1. Check Redis cache
        String cachedOrder =
                redisTemplate.opsForValue().get(cacheKey);

        if (cachedOrder != null) {
            try {
                log.info("REDIS CACHE HIT for order: " + orderId);

                return objectMapper.readValue(
                        cachedOrder,
                        OrderResponse.class);

            } catch (JsonProcessingException exception) {
                // Invalid cached data: remove it and fetch again.
                redisTemplate.delete(cacheKey);
            }
        }

        log.info("REDIS CACHE MISS for order: " + orderId);

        // 2. Obtain JWT
        String token = jwtTokenService.getAccessToken();

        // 3. Call API Gateway
        OrderResponse order = orderClient.get()
                .uri("/api/v1/orders/{id}", orderId)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token)
                .retrieve()
                .body(OrderResponse.class);

        if (order == null) {
            throw new IllegalStateException(
                    "No order returned for ID: " + orderId);
        }

        // 4. Store complete order response in Redis
        try {
            String json =
                    objectMapper.writeValueAsString(order);

            redisTemplate.opsForValue().set(
                    cacheKey,
                    json,
                    CACHE_TTL);

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Unable to serialize order for Redis",
                    exception);
        }

        return order;
    }
}
