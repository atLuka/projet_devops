package com.microservices.userservice.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiErrorTest {

    @Test
    void constructorPopulatesFieldsAndTimestamp() {
        ApiError error = new ApiError(404, "Not Found", "missing", "user-service");

        assertThat(error.getStatus()).isEqualTo(404);
        assertThat(error.getError()).isEqualTo("Not Found");
        assertThat(error.getMessage()).isEqualTo("missing");
        assertThat(error.getService()).isEqualTo("user-service");
        assertThat(error.getTimestamp()).isNotNull();
    }

    @Test
    void settersUpdateFields() {
        ApiError error = new ApiError(0, null, null, null);
        error.setStatus(400);
        error.setError("Bad Request");
        error.setMessage("invalid");
        error.setService("svc");
        error.setTimestamp("2026-01-01T00:00");

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getError()).isEqualTo("Bad Request");
        assertThat(error.getMessage()).isEqualTo("invalid");
        assertThat(error.getService()).isEqualTo("svc");
        assertThat(error.getTimestamp()).isEqualTo("2026-01-01T00:00");
    }
}
