package com.microservices.authservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    // 64-byte base64 secret, large enough for HS256/384/512
    private static final String SECRET =
        "dGhpcyBpcyBhIHZlcnkgc2VjcmV0IGtleSBmb3IgSldUIHRva2VuIGdlbmVyYXRpb24gMTIzNDU2Nzg5MA==";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 86_400_000L);
    }

    @Test
    void generateToken_isParsableAndCarriesClaims() {
        String token = jwtService.generateToken("john@example.com", "TENANT");

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(jwtService.getEmailFromToken(token))
            .isEqualTo("john@example.com");
        assertThat(jwtService.getRoleFromToken(token)).isEqualTo("TENANT");
        assertThat(jwtService.validateToken(token).getSubject())
            .isEqualTo("john@example.com");
    }

    @Test
    void validateToken_rejectsTamperedToken() {
        String token = jwtService.generateToken("john@example.com", "TENANT");
        String tampered = token + "abc";

        assertThatThrownBy(() -> jwtService.validateToken(tampered))
            .isInstanceOf(JwtException.class);
    }

    @Test
    void validateToken_rejectsTokenSignedWithAnotherKey() {
        JwtService other = new JwtService(
            "YW5vdGhlciB2ZXJ5IHNlY3JldCBrZXkgZm9yIEpXVCBzaWduaW5nIDk4NzY1NDMyMTA=",
            86_400_000L
        );
        String foreignToken = other.generateToken("john@example.com", "OWNER");

        assertThatThrownBy(() -> jwtService.validateToken(foreignToken))
            .isInstanceOf(JwtException.class);
    }
}
