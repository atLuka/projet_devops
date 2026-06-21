package com.microservices.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Couvre les branches restantes du filtre (health, POST properties, role absent). */
class JwtAuthFilterBranchTest {

    private static final String SECRET =
        "dGhpcyBpcyBhIHZlcnkgc2VjcmV0IGtleSBmb3IgSldUIHRva2VuIGdlbmVyYXRpb24gMTIzNDU2Nzg5MA==";

    private JwtAuthFilter filter;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(SECRET);
        key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));
    }

    private static class CapturingChain implements GatewayFilterChain {

        final AtomicReference<ServerWebExchange> captured = new AtomicReference<>();

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            captured.set(exchange);
            return Mono.empty();
        }
    }

    @Test
    void passesThrough_whenHealthEndpoint() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/users/health")
        );
        CapturingChain chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.captured.get()).isNotNull();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void returns401_whenPostProperties_withoutToken() {
        // Seul le GET sur /api/properties est public : un POST doit exiger un token.
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/api/properties")
        );
        CapturingChain chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.captured.get()).isNull();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(
            HttpStatus.UNAUTHORIZED
        );
    }

    @Test
    void forwardsWithEmptyRoleHeader_whenRoleClaimMissing() {
        String token = Jwts.builder()
            .subject("john@example.com")
            .signWith(key)
            .compact();
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/users/1").header(
                "Authorization",
                "Bearer " + token
            )
        );
        CapturingChain chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        ServerWebExchange forwarded = chain.captured.get();
        assertThat(forwarded).isNotNull();
        assertThat(
            forwarded.getRequest().getHeaders().getFirst("X-User-Email")
        ).isEqualTo("john@example.com");
        assertThat(
            forwarded.getRequest().getHeaders().getFirst("X-User-Role")
        ).isEqualTo("");
    }
}
