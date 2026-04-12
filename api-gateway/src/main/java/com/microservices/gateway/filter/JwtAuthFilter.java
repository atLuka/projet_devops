package com.microservices.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final SecretKey secretKey;

    public JwtAuthFilter(
        @org.springframework.beans.factory.annotation.Value(
            "${jwt.secret}"
        ) String secretBase64
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(secretBase64);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public Mono<Void> filter(
        ServerWebExchange exchange,
        GatewayFilterChain chain
    ) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }
        if (path.endsWith("/health")) {
            return chain.filter(exchange);
        }
        if (path.startsWith("/api/auth")) {
            return chain.filter(exchange);
        }
        if (
            path.startsWith("/api/properties") &&
            request.getMethod() == HttpMethod.GET
        ) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            ServerHttpRequest modifiedRequest = request
                .mutate()
                .header("X-User-Email", email)
                .header("X-User-Role", role != null ? role : "")
                .build();

            return chain.filter(
                exchange.mutate().request(modifiedRequest).build()
            );
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
