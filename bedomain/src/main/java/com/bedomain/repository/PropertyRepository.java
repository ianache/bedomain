package com.bedomain.repository;

import com.bedomain.domain.entity.Property;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyRepository extends org.springframework.data.jpa.repository.JpaRepository<Property, UUID> {

    List<Property> findByEntityTypeId(UUID entityTypeId);

    Optional<Property> findByIdAndEntityTypeId(UUID id, UUID entityTypeId);

    boolean existsByEntityTypeIdAndName(UUID entityTypeId, String name);
}
