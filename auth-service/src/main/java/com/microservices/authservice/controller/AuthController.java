package com.microservices.authservice.controller;

import com.microservices.authservice.dto.ApiError;
import com.microservices.authservice.dto.AuthResponse;
import com.microservices.authservice.dto.LoginRequest;
import com.microservices.authservice.dto.RegisterRequest;
import com.microservices.authservice.dto.UserDto;
import com.microservices.authservice.service.JwtService;
import com.microservices.authservice.service.UserServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(
        AuthController.class
    );

    private final UserServiceClient userServiceClient;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(
        UserServiceClient userServiceClient,
        JwtService jwtService
    ) {
        this.userServiceClient = userServiceClient;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        log.info(
            "Tentative d'inscription pour l'email: {}",
            sanitize(request.getEmail())
        );
        UserDto user = userServiceClient.createUser(request);
        String token = jwtService.generateToken(
            user.getEmail(),
            user.getRole()
        );
        AuthResponse response = new AuthResponse(
            token,
            user.getEmail(),
            user.getRole(),
            user.getId()
        );
        log.info("Inscription reussie pour l'email: {}", sanitize(user.getEmail()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("Tentative de connexion pour l'email: {}", sanitize(request.getEmail()));
        UserDto user = userServiceClient.getUserByEmail(request.getEmail());

        if (
            user == null ||
            !passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
            )
        ) {
            log.warn("Echec de connexion pour l'email: {}", sanitize(request.getEmail()));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ApiError(
                    401,
                    "Unauthorized",
                    "Email ou mot de passe incorrect",
                    "auth-service"
                )
            );
        }

        String token = jwtService.generateToken(
            user.getEmail(),
            user.getRole()
        );
        AuthResponse response = new AuthResponse(
            token,
            user.getEmail(),
            user.getRole(),
            user.getId()
        );
        log.info("Connexion reussie pour l'email: {}", sanitize(user.getEmail()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public java.util.Map<String, String> health() {
        return java.util.Map.of("status", "UP", "service", "auth-service");
    }

    // Neutralise les retours chariot/sauts de ligne pour éviter l'injection de logs (CRLF).
    private static String sanitize(String value) {
        return value == null ? "null" : value.replaceAll("[\\r\\n]", "_");
    }
}
