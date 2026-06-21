package com.microservices.authservice.service;

import com.microservices.authservice.dto.RegisterRequest;
import com.microservices.authservice.dto.UserDto;
import com.microservices.authservice.exception.ServiceUnavailableException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(
        UserServiceClient.class
    );

    private final RestTemplate restTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${user.service.url}")
    private String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public UserDto getUserByEmail(String email) {
        try {
            // L'email est passé en variable de template : RestTemplate l'encode
            // dans le chemin (pas de concaténation d'entrée utilisateur brute).
            String url = userServiceUrl + "/users/email/{email}";
            log.info("Appel au service utilisateur: GET {}/users/email/...", userServiceUrl);
            ResponseEntity<UserDto> response = restTemplate.getForEntity(
                url,
                UserDto.class,
                email
            );
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (RestClientException e) {
            log.error(
                "Erreur lors de l'appel au service utilisateur: {}",
                e.getMessage()
            );
            throw new ServiceUnavailableException(
                "Le service utilisateur est indisponible"
            );
        }
    }

    public UserDto createUser(RegisterRequest request) {
        try {
            String url = userServiceUrl + "/users";
            log.info("Appel au service utilisateur: POST {}", url);

            Map<String, String> body = new HashMap<>();
            body.put("email", request.getEmail());
            body.put(
                "passwordHash",
                passwordEncoder.encode(request.getPassword())
            );
            body.put("role", request.getRole());
            body.put("firstName", request.getFirstName());
            body.put("lastName", request.getLastName());
            body.put("phone", request.getPhone());

            ResponseEntity<UserDto> response = restTemplate.postForEntity(
                url,
                body,
                UserDto.class
            );
            return response.getBody();
        } catch (RestClientException e) {
            log.error(
                "Erreur lors de la creation d'utilisateur: {}",
                e.getMessage()
            );
            throw new ServiceUnavailableException(
                "Le service utilisateur est indisponible"
            );
        }
    }
}
