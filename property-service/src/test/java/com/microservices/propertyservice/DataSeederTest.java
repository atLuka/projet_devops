package com.microservices.propertyservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microservices.propertyservice.model.Property;
import com.microservices.propertyservice.model.Reservation;
import com.microservices.propertyservice.repository.PropertyRepository;
import com.microservices.propertyservice.repository.ReservationRepository;
import org.junit.jupiter.api.Test;

class DataSeederTest {

    @Test
    void seedsPropertiesAndReservations_whenDatabaseEmpty() {
        PropertyRepository propertyRepo = mock(PropertyRepository.class);
        ReservationRepository reservationRepo = mock(ReservationRepository.class);
        when(propertyRepo.count()).thenReturn(0L);

        new DataSeeder(propertyRepo, reservationRepo).run();

        verify(propertyRepo, times(50)).save(any(Property.class));
        verify(reservationRepo, times(30)).save(any(Reservation.class));
    }

    @Test
    void skipsSeeding_whenDataAlreadyExists() {
        PropertyRepository propertyRepo = mock(PropertyRepository.class);
        ReservationRepository reservationRepo = mock(ReservationRepository.class);
        when(propertyRepo.count()).thenReturn(50L);

        new DataSeeder(propertyRepo, reservationRepo).run();

        verify(propertyRepo, never()).save(any(Property.class));
        verify(reservationRepo, never()).save(any(Reservation.class));
    }
}
