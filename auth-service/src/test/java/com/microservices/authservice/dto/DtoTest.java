package com.microservices.authservice.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DtoTest {

    @Test
    void apiError_constructorAndSetters() {
        ApiError error = new ApiError(404, "Not Found", "missing", "auth-service");
        assertThat(error.getStatus()).isEqualTo(404);
        assertThat(error.getError()).isEqualTo("Not Found");
        assertThat(error.getMessage()).isEqualTo("missing");
        assertThat(error.getService()).isEqualTo("auth-service");
        assertThat(error.getTimestamp()).isNotNull();

        error.setStatus(500);
        error.setError("Internal Server Error");
        error.setMessage("boom");
        error.setService("svc");
        error.setTimestamp("2026-01-01T00:00");
        assertThat(error.getStatus()).isEqualTo(500);
        assertThat(error.getError()).isEqualTo("Internal Server Error");
        assertThat(error.getMessage()).isEqualTo("boom");
        assertThat(error.getService()).isEqualTo("svc");
        assertThat(error.getTimestamp()).isEqualTo("2026-01-01T00:00");
    }

    @Test
    void authResponse_constructorAndSetters() {
        AuthResponse r = new AuthResponse("tok", "a@b.c", "TENANT", 7L);
        assertThat(r.getToken()).isEqualTo("tok");
        assertThat(r.getEmail()).isEqualTo("a@b.c");
        assertThat(r.getRole()).isEqualTo("TENANT");
        assertThat(r.getUserId()).isEqualTo(7L);

        AuthResponse empty = new AuthResponse();
        empty.setToken("t");
        empty.setEmail("e@x.y");
        empty.setRole("OWNER");
        empty.setUserId(1L);
        assertThat(empty.getToken()).isEqualTo("t");
        assertThat(empty.getEmail()).isEqualTo("e@x.y");
        assertThat(empty.getRole()).isEqualTo("OWNER");
        assertThat(empty.getUserId()).isEqualTo(1L);
    }

    @Test
    void loginRequest_gettersSetters() {
        LoginRequest req = new LoginRequest();
        req.setEmail("a@b.c");
        req.setPassword("pw");
        assertThat(req.getEmail()).isEqualTo("a@b.c");
        assertThat(req.getPassword()).isEqualTo("pw");
    }

    @Test
    void registerRequest_gettersSetters() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("a@b.c");
        req.setPassword("pw");
        req.setRole("OWNER");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setPhone("0600000000");
        assertThat(req.getEmail()).isEqualTo("a@b.c");
        assertThat(req.getPassword()).isEqualTo("pw");
        assertThat(req.getRole()).isEqualTo("OWNER");
        assertThat(req.getFirstName()).isEqualTo("John");
        assertThat(req.getLastName()).isEqualTo("Doe");
        assertThat(req.getPhone()).isEqualTo("0600000000");
    }

    @Test
    void userDto_gettersSetters() {
        UserDto dto = new UserDto();
        dto.setId(3L);
        dto.setEmail("a@b.c");
        dto.setPasswordHash("hash");
        dto.setRole("TENANT");
        dto.setFirstName("Jane");
        dto.setLastName("Roe");
        dto.setPhone("0700000000");
        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getEmail()).isEqualTo("a@b.c");
        assertThat(dto.getPasswordHash()).isEqualTo("hash");
        assertThat(dto.getRole()).isEqualTo("TENANT");
        assertThat(dto.getFirstName()).isEqualTo("Jane");
        assertThat(dto.getLastName()).isEqualTo("Roe");
        assertThat(dto.getPhone()).isEqualTo("0700000000");
    }
}
