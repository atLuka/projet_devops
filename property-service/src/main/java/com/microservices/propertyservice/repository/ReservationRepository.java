package com.microservices.propertyservice.repository;

import com.microservices.propertyservice.model.Reservation;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository
    extends JpaRepository<Reservation, Long>
{
    List<Reservation> findByPropertyId(Long propertyId);

    List<Reservation> findByTenantId(Long tenantId);

    List<Reservation> findByPropertyIdAndStatusNot(
        Long propertyId,
        String status
    );

    @Query(
        "SELECT r FROM Reservation r WHERE r.propertyId = :propertyId " +
            "AND r.status <> 'CANCELLED' " +
            "AND r.startDate < :endDate AND r.endDate > :startDate"
    )
    List<Reservation> findOverlapping(
        @Param("propertyId") Long propertyId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
