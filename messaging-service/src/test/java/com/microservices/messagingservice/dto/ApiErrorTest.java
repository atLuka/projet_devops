package com.microservices.messagingservice.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ApiErrorTest {

    @Test
    void constructorPopulatesFieldsAndTimestamp() {
        ApiError error = new ApiError(404, "Not Found", "missing", "messaging-service");

        assertThat(error.getStatus()).isEqualTo(404);
        assertThat(error.getError()).isEqualTo("Not Found");
        assertThat(error.getMessage()).isEqualTo("missing");
        assertThat(error.getService()).isEqualTo("messaging-service");
        assertThat(error.getTimestamp()).isNotNull();
    }

    @Test
    void settersUpdateFields() {
        ApiError error = new ApiError(0, null, null, null);
        LocalDateTime ts = LocalDateTime.of(2026, 1, 1, 0, 0);
        error.setStatus(500);
        error.setError("Internal Server Error");
        error.setMessage("boom");
        error.setService("svc");
        error.setTimestamp(ts);

        assertThat(error.getStatus()).isEqualTo(500);
        assertThat(error.getError()).isEqualTo("Internal Server Error");
        assertThat(error.getMessage()).isEqualTo("boom");
        assertThat(error.getService()).isEqualTo("svc");
        assertThat(error.getTimestamp()).isEqualTo(ts);
    }
}
