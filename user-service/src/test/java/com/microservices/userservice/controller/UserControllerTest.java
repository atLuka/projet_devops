package com.microservices.userservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.userservice.model.User;
import com.microservices.userservice.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    private User sampleUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setPasswordHash("hash");
        user.setRole("TENANT");
        user.setFirstName("John");
        user.setLastName("Doe");
        return user;
    }

    @Test
    void getUserById_returnsUser_whenFound() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser()));

        mockMvc
            .perform(get("/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("john@example.com"))
            .andExpect(jsonPath("$.role").value("TENANT"));
    }

    @Test
    void getUserById_returns404_whenMissing() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc
            .perform(get("/users/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.service").value("user-service"));
    }

    @Test
    void getUserByEmail_returnsUser_whenFound() throws Exception {
        when(userRepository.findByEmail("john@example.com"))
            .thenReturn(Optional.of(sampleUser()));

        mockMvc
            .perform(get("/users/email/john@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getUserByEmail_returns404_whenMissing() throws Exception {
        when(userRepository.findByEmail("nobody@example.com"))
            .thenReturn(Optional.empty());

        mockMvc
            .perform(get("/users/email/nobody@example.com"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createUser_persistsUser_whenEmailFree() throws Exception {
        User input = sampleUser();
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(input);

        mockMvc
            .perform(
                post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void createUser_returns400_whenEmailAlreadyUsed() throws Exception {
        User input = sampleUser();
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        mockMvc
            .perform(
                post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getAllUsers_returnsList() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser()));

        mockMvc
            .perform(get("/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].email").value("john@example.com"));
    }

    @Test
    void health_returnsUp() throws Exception {
        mockMvc
            .perform(get("/users/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("user-service"));
    }
}
