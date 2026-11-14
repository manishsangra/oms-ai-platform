package com.example.orders.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            Converter<Jwt, Mono<AbstractAuthenticationToken>>
                    jwtAuthenticationConverter) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        // Product read access
                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/v1/products/**"
                        ).hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                        // Product write access - ADMIN only
                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/v1/products/**"
                        ).hasAuthority("ROLE_ADMIN")
                        .pathMatchers(
                                HttpMethod.PUT,
                                "/api/v1/products/**"
                        ).hasAuthority("ROLE_ADMIN")
                        .pathMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/products/**"
                        ).hasAuthority("ROLE_ADMIN")
                        // All authenticated users can access orders
                        .pathMatchers(
                                "/api/v1/orders/**"
                        ).hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                        // Everything else requires authentication
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                )
                .build();
    }
}
