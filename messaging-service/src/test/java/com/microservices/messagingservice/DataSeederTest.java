package com.microservices.messagingservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microservices.messagingservice.model.Message;
import com.microservices.messagingservice.repository.MessageRepository;
import org.junit.jupiter.api.Test;

class DataSeederTest {

    @Test
    void seedsMessages_whenDatabaseEmpty() {
        MessageRepository repo = mock(MessageRepository.class);
        when(repo.count()).thenReturn(0L);

        new DataSeeder(repo).run();

        verify(repo, times(19)).save(any(Message.class));
    }

    @Test
    void skipsSeeding_whenMessagesAlreadyExist() {
        MessageRepository repo = mock(MessageRepository.class);
        when(repo.count()).thenReturn(5L);

        new DataSeeder(repo).run();

        verify(repo, never()).save(any(Message.class));
    }
}
