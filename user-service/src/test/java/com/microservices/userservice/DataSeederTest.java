package com.microservices.userservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microservices.userservice.model.User;
import com.microservices.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DataSeederTest {

    @Test
    void seedsFourteenUsers_whenDatabaseEmpty() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.count()).thenReturn(0L);

        DataSeeder seeder = new DataSeeder(repo);
        ReflectionTestUtils.setField(seeder, "seedPassword", "password123");
        seeder.run();

        verify(repo, times(14)).save(any(User.class));
    }

    @Test
    void skipsSeeding_whenUsersAlreadyExist() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.count()).thenReturn(14L);

        new DataSeeder(repo).run();

        verify(repo, never()).save(any(User.class));
    }
}
