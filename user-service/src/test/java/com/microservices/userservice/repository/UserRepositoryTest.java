package com.microservices.userservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.microservices.userservice.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:userdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    }
)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User newUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole("TENANT");
        user.setFirstName("John");
        user.setLastName("Doe");
        return user;
    }

    @Test
    void savesAndFindsByEmail() {
        userRepository.save(newUser("alice@example.com"));

        assertThat(userRepository.findByEmail("alice@example.com"))
            .isPresent()
            .get()
            .extracting(User::getRole)
            .isEqualTo("TENANT");
    }

    @Test
    void findByEmail_isEmpty_whenUnknown() {
        assertThat(userRepository.findByEmail("ghost@example.com")).isEmpty();
    }

    @Test
    void existsByEmail_reflectsPersistence() {
        userRepository.save(newUser("bob@example.com"));

        assertThat(userRepository.existsByEmail("bob@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("none@example.com")).isFalse();
    }
}
