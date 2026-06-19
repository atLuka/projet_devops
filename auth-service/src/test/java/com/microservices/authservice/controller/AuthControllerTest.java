package com.microservices.authservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.authservice.dto.LoginRequest;
import com.microservices.authservice.dto.RegisterRequest;
import com.microservices.authservice.dto.UserDto;
import com.microservices.authservice.service.JwtService;
import com.microservices.authservice.service.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceClient userServiceClient;

    @MockBean
    private JwtService jwtService;

    private UserDto user(String passwordHash) {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setRole("TENANT");
        user.setPasswordHash(passwordHash);
        return user;
    }

    @Test
    void register_returnsTokenAndUserInfo() throws Exception {
        when(userServiceClient.createUser(any(RegisterRequest.class)))
            .thenReturn(user("ignored"));
        when(jwtService.generateToken("john@example.com", "TENANT"))
            .thenReturn("jwt-token");

        RegisterRequest request = new RegisterRequest();
        request.setEmail("john@example.com");
        request.setPassword("secret");
        request.setRole("TENANT");

        mockMvc
            .perform(
                post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.email").value("john@example.com"))
            .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void login_returnsToken_whenCredentialsValid() throws Exception {
        String hash = new BCryptPasswordEncoder().encode("secret");
        when(userServiceClient.getUserByEmail("john@example.com"))
            .thenReturn(user(hash));
        when(jwtService.generateToken("john@example.com", "TENANT"))
            .thenReturn("jwt-token");

        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("secret");

        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_returns401_whenPasswordWrong() throws Exception {
        String hash = new BCryptPasswordEncoder().encode("realpassword");
        when(userServiceClient.getUserByEmail("john@example.com"))
            .thenReturn(user(hash));

        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrong");

        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Email ou mot de passe incorrect"));
    }

    @Test
    void login_returns401_whenUserUnknown() throws Exception {
        when(userServiceClient.getUserByEmail(eq("ghost@example.com")))
            .thenReturn(null);

        LoginRequest request = new LoginRequest();
        request.setEmail("ghost@example.com");
        request.setPassword("secret");

        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isUnauthorized());
    }
}
