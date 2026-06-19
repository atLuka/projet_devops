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

class JwtAuthFilterTest {

    // 64-byte base64 secret, large enough for HS256
    private static final String SECRET =
        "dGhpcyBpcyBhIHZlcnkgc2VjcmV0IGtleSBmb3IgSldUIHRva2VuIGdlbmVyYXRpb24gMTIzNDU2Nzg5MA==";

    private JwtAuthFilter filter;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(SECRET);
        key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));
    }

    private String validToken(String email, String role) {
        return Jwts.builder()
            .subject(email)
            .claim("role", role)
            .signWith(key)
            .compact();
    }

    /** Chain that records the exchange it received so we can assert pass-through. */
    private static class CapturingChain implements GatewayFilterChain {

        final AtomicReference<ServerWebExchange> captured = new AtomicReference<>();

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            captured.set(exchange);
            return Mono.empty();
        }
    }

    @Test
    void passesThrough_whenPathNotUnderApi() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/internal/metrics")
        );
        CapturingChain chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.captured.get()).isNotNull();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void passesThrough_whenAuthEndpoint() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/api/auth/login")
        );
        CapturingChain chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.captured.get()).isNotNull();
    }

    @Test
    void passesThrough_whenGetProperties() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/properties")
        );
        CapturingChain chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.captured.get()).isNotNull();
    }

    @Test
    void returns401_whenAuthorizationHeaderMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/users/1")
        );
        CapturingChain chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.captured.get()).isNull();
        assertThat(exchange.getResponse().getStatusCode())
            .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void returns401_whenTokenInvalid() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/users/1")
                .header("Authorization", "Bearer not-a-jwt")
        );
        CapturingChain chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.captured.get()).isNull();
        assertThat(exchange.getResponse().getStatusCode())
            .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void forwardsWithUserHeaders_whenTokenValid() {
        String token = validToken("john@example.com", "TENANT");
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/users/1")
                .header("Authorization", "Bearer " + token)
        );
        CapturingChain chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        ServerWebExchange forwarded = chain.captured.get();
        assertThat(forwarded).isNotNull();
        assertThat(forwarded.getRequest().getHeaders().getFirst("X-User-Email"))
            .isEqualTo("john@example.com");
        assertThat(forwarded.getRequest().getHeaders().getFirst("X-User-Role"))
            .isEqualTo("TENANT");
    }

    @Test
    void getOrder_isOne() {
        assertThat(filter.getOrder()).isEqualTo(1);
    }
}
