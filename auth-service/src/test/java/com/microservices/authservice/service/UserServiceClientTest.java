package com.microservices.authservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.microservices.authservice.dto.RegisterRequest;
import com.microservices.authservice.dto.UserDto;
import com.microservices.authservice.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class UserServiceClientTest {

    private static final String BASE_URL = "http://user-service:8443";

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private UserServiceClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new UserServiceClient(restTemplate);
        ReflectionTestUtils.setField(client, "userServiceUrl", BASE_URL);
    }

    @Test
    void getUserByEmail_returnsUser_onSuccess() {
        server
            .expect(requestTo(BASE_URL + "/users/email/john@example.com"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withSuccess(
                    "{\"id\":1,\"email\":\"john@example.com\",\"role\":\"TENANT\"}",
                    MediaType.APPLICATION_JSON
                )
            );

        UserDto user = client.getUserByEmail("john@example.com");

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getRole()).isEqualTo("TENANT");
        server.verify();
    }

    @Test
    void getUserByEmail_returnsNull_whenNotFound() {
        server
            .expect(requestTo(BASE_URL + "/users/email/ghost@example.com"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.getUserByEmail("ghost@example.com")).isNull();
        server.verify();
    }

    @Test
    void getUserByEmail_throwsServiceUnavailable_onServerError() {
        server
            .expect(requestTo(BASE_URL + "/users/email/john@example.com"))
            .andRespond(withServerError());

        assertThatThrownBy(() -> client.getUserByEmail("john@example.com"))
            .isInstanceOf(ServiceUnavailableException.class);
        server.verify();
    }

    @Test
    void createUser_hashesPasswordAndReturnsUser() {
        server
            .expect(requestTo(BASE_URL + "/users"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withSuccess(
                    "{\"id\":2,\"email\":\"new@example.com\",\"role\":\"OWNER\"}",
                    MediaType.APPLICATION_JSON
                )
            );

        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("secret");
        request.setRole("OWNER");

        UserDto created = client.createUser(request);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(2L);
        server.verify();
    }

    @Test
    void createUser_throwsServiceUnavailable_onServerError() {
        server
            .expect(requestTo(BASE_URL + "/users"))
            .andRespond(withServerError());

        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("secret");

        assertThatThrownBy(() -> client.createUser(request))
            .isInstanceOf(ServiceUnavailableException.class);
        server.verify();
    }
}
