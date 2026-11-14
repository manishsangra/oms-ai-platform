package com.example.orders.product.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class JwtAuthenticationConverterConfig {

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken>
    jwtAuthenticationConverter() {

        return jwt -> {
            List<String> roles = jwt.getClaimAsStringList("roles");

            var authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            return new JwtAuthenticationToken(
                    jwt,
                    authorities,
                    jwt.getSubject()
            );
        };
    }
}
