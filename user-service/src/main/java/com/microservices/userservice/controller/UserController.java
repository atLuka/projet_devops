package com.microservices.userservice.controller;

import com.microservices.userservice.exception.EntityNotFoundException;
import com.microservices.userservice.model.User;
import com.microservices.userservice.repository.UserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(
        UserController.class
    );

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        log.info("Recherche utilisateur par id : {}", id);
        return userRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Utilisateur non trouvé : " + id)
            );
    }

    @GetMapping("/email/{email}")
    public User getUserByEmail(@PathVariable String email) {
        log.info("Recherche utilisateur par email : {}", email);
        return userRepository
            .findByEmail(email)
            .orElseThrow(() ->
                new EntityNotFoundException("Utilisateur non trouvé : " + email)
            );
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Création utilisateur avec email : {}", user.getEmail());
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                "Email déjà utilisé : " + user.getEmail()
            );
        }
        return userRepository.save(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Récupération de tous les utilisateurs");
        return userRepository.findAll();
    }
}
