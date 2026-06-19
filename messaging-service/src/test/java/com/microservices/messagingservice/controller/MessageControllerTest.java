package com.microservices.messagingservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.messagingservice.model.Message;
import com.microservices.messagingservice.repository.MessageRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageRepository messageRepository;

    private Message message(Long id, String role, boolean read) {
        Message m = new Message();
        m.setId(id);
        m.setReservationId(1L);
        m.setSenderId(5L);
        m.setSenderRole(role);
        m.setContent("Bonjour");
        m.setSentAt(LocalDateTime.now());
        m.setRead(read);
        return m;
    }

    @Test
    void getByReservation_returnsOrderedMessages() throws Exception {
        when(
            messageRepository.findByReservationIdOrderBySentAtAsc(1L)
        ).thenReturn(List.of(message(1L, "TENANT", true)));

        mockMvc
            .perform(get("/messages/reservation/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].senderRole").value("TENANT"));
    }

    @Test
    void sendMessage_setsSentAtAndUnread() throws Exception {
        Message input = message(null, "TENANT", true);
        when(messageRepository.save(any(Message.class))).thenAnswer(inv ->
            inv.getArgument(0)
        );

        mockMvc
            .perform(
                post("/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.read").value(false));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().isRead()).isFalse();
        assertThat(captor.getValue().getSentAt()).isNotNull();
    }

    @Test
    void getUnreadCounts_aggregatesPerReservation() throws Exception {
        when(
            messageRepository.countByReservationIdAndReadFalseAndSenderRoleNot(
                eq(1L),
                eq("TENANT")
            )
        ).thenReturn(2L);
        when(
            messageRepository.countByReservationIdAndReadFalseAndSenderRoleNot(
                eq(2L),
                eq("TENANT")
            )
        ).thenReturn(0L);

        mockMvc
            .perform(
                get("/messages/unread")
                    .param("role", "TENANT")
                    .param("reservationIds", "1", "2")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(2))
            .andExpect(jsonPath("$.1").value(2))
            .andExpect(jsonPath("$.2").doesNotExist());
    }

    @Test
    void markAsRead_updatesOnlyOtherRoleUnreadMessages() throws Exception {
        Message fromOwnerUnread = message(10L, "OWNER", false);
        Message fromOwnerRead = message(11L, "OWNER", true);
        Message fromTenant = message(12L, "TENANT", false);
        when(
            messageRepository.findByReservationIdOrderBySentAtAsc(1L)
        ).thenReturn(List.of(fromOwnerUnread, fromOwnerRead, fromTenant));

        mockMvc
            .perform(
                put("/messages/reservation/1/read").param("role", "TENANT")
            )
            .andExpect(status().isOk());

        // only the unread OWNER message must be saved back
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(messageRepository, never()).save(fromTenant);
        assertThat(fromOwnerUnread.isRead()).isTrue();
    }

    @Test
    void health_returnsUp() throws Exception {
        mockMvc
            .perform(get("/messages/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.service").value("messaging-service"));
    }
}
