package com.microservices.propertyservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.propertyservice.model.Property;
import com.microservices.propertyservice.model.Reservation;
import com.microservices.propertyservice.repository.PropertyRepository;
import com.microservices.propertyservice.repository.ReservationRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationRepository reservationRepository;

    @MockBean
    private PropertyRepository propertyRepository;

    private Reservation reservation(Long propertyId) {
        Reservation r = new Reservation();
        r.setPropertyId(propertyId);
        r.setTenantId(3L);
        r.setStartDate(LocalDate.of(2025, 8, 1));
        r.setEndDate(LocalDate.of(2025, 8, 7));
        return r;
    }

    @Test
    void createReservation_savesWithPendingStatus_whenNoOverlap() throws Exception {
        when(
            reservationRepository.findOverlapping(
                eq(1L),
                any(LocalDate.class),
                any(LocalDate.class)
            )
        ).thenReturn(List.of());
        when(reservationRepository.save(any(Reservation.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc
            .perform(
                post("/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reservation(1L)))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PENDING"));

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(
            Reservation.class
        );
        verify(reservationRepository).save(captor.capture());
        org.assertj.core.api.Assertions
            .assertThat(captor.getValue().getStatus())
            .isEqualTo("PENDING");
    }

    @Test
    void createReservation_returns409_whenOverlap() throws Exception {
        when(
            reservationRepository.findOverlapping(
                eq(1L),
                any(LocalDate.class),
                any(LocalDate.class)
            )
        ).thenReturn(List.of(reservation(1L)));

        mockMvc
            .perform(
                post("/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reservation(1L)))
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void getByTenant_delegatesToRepository() throws Exception {
        when(reservationRepository.findByTenantId(3L))
            .thenReturn(List.of(reservation(1L)));

        mockMvc
            .perform(get("/reservations/tenant/3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].tenantId").value(3));
    }

    @Test
    void getByOwner_filtersReservationsToOwnedProperties() throws Exception {
        Property owned = new Property();
        owned.setId(1L);
        owned.setOwnerId(9L);
        when(propertyRepository.findByOwnerId(9L)).thenReturn(List.of(owned));
        when(reservationRepository.findAll())
            .thenReturn(List.of(reservation(1L), reservation(2L)));

        mockMvc
            .perform(get("/reservations/owner/9"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].propertyId").value(1));
    }

    @Test
    void updateStatus_updatesExistingReservation() throws Exception {
        Reservation existing = reservation(1L);
        existing.setId(10L);
        existing.setStatus("PENDING");
        when(reservationRepository.findById(10L))
            .thenReturn(Optional.of(existing));
        when(reservationRepository.save(any(Reservation.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc
            .perform(
                put("/reservations/10/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("status", "CONFIRMED")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void updateStatus_returns404_whenMissing() throws Exception {
        when(reservationRepository.findById(404L)).thenReturn(Optional.empty());

        mockMvc
            .perform(
                put("/reservations/404/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("status", "CONFIRMED")))
            )
            .andExpect(status().isNotFound());
    }
}
