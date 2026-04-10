package com.microservices.propertyservice.repository;

import com.microservices.propertyservice.model.Property;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByOwnerId(Long ownerId);

    List<Property> findByType(String type);

    List<Property> findByLocationContainingIgnoreCase(String location);

    List<Property> findByPriceLessThanEqual(Double maxPrice);

    @Query(
        "SELECT p FROM Property p WHERE " +
            "(:type IS NULL OR p.type = :type) AND " +
            "(:location IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)"
    )
    List<Property> searchProperties(
        @Param("type") String type,
        @Param("location") String location,
        @Param("maxPrice") Double maxPrice
    );
}
