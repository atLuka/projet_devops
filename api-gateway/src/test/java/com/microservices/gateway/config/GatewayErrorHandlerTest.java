package com.microservices.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;

class GatewayErrorHandlerTest {

    private GatewayErrorHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GatewayErrorHandler();
    }

    private MockServerWebExchange exchange(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path));
    }

    @Test
    void mapsResponseStatusException_toItsStatus() {
        MockServerWebExchange exchange = exchange("/api/users/1");

        handler
            .handle(
                exchange,
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Interdit")
            )
            .block();

        assertThat(exchange.getResponse().getStatusCode())
            .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchange.getResponse().getHeaders().getContentType())
            .isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(exchange.getResponse().getBodyAsString().block())
            .contains("Interdit");
    }

    @Test
    void mapsConnectionError_to503WithGuessedService() {
        MockServerWebExchange exchange = exchange("/api/users/1");

        handler
            .handle(exchange, new RuntimeException("Connection refused"))
            .block();

        assertThat(exchange.getResponse().getStatusCode())
            .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains("user-service");
        assertThat(body).contains("Service indisponible");
    }

    @Test
    void mapsConnectionError_forPropertyPath() {
        MockServerWebExchange exchange = exchange("/api/properties/1");

        handler
            .handle(exchange, new RuntimeException("Connection refused"))
            .block();

        assertThat(exchange.getResponse().getBodyAsString().block())
            .contains("property-service");
    }

    @Test
    void mapsUnknownError_to500() {
        MockServerWebExchange exchange = exchange("/api/users/1");

        handler
            .handle(exchange, new RuntimeException("boom"))
            .block();

        assertThat(exchange.getResponse().getStatusCode())
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exchange.getResponse().getBodyAsString().block())
            .contains("Erreur interne");
    }
}
