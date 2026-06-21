package com.microservices.authservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.microservices.authservice.dto.ApiError;
import com.microservices.authservice.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        ReflectionTestUtils.setField(handler, "serviceName", "auth-service");
    }

    @Test
    void handleServiceUnavailable_returns503() {
        ResponseEntity<ApiError> response = handler.handleServiceUnavailable(
            new ServiceUnavailableException("user-service down")
        );

        assertThat(response.getStatusCode()).isEqualTo(
            HttpStatus.SERVICE_UNAVAILABLE
        );
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(503);
        assertThat(body.getError()).isEqualTo("Service Unavailable");
        assertThat(body.getMessage()).isEqualTo("user-service down");
        assertThat(body.getService()).isEqualTo("auth-service");
        assertThat(body.getTimestamp()).isNotNull();
    }

    @Test
    void handleIllegalArgument_returns400() {
        ResponseEntity<ApiError> response = handler.handleIllegalArgument(
            new IllegalArgumentException("bad input")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("bad input");
    }

    @Test
    void handleGenericException_returns500WithMaskedMessage() {
        ResponseEntity<ApiError> response = handler.handleGenericException(
            new RuntimeException("internal stacktrace detail")
        );

        assertThat(response.getStatusCode()).isEqualTo(
            HttpStatus.INTERNAL_SERVER_ERROR
        );
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        // Le detail technique ne doit pas fuiter vers le client
        assertThat(response.getBody().getMessage()).isEqualTo(
            "Une erreur interne est survenue"
        );
    }
}
