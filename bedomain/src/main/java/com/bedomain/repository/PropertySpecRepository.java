package com.bedomain.repository;

import com.bedomain.entity.PropertySpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertySpecRepository extends JpaRepository<PropertySpec, UUID> {

    List<PropertySpec> findByEntityTypeId(UUID entityTypeId);

    boolean existsByEntityTypeIdAndName(UUID entityTypeId, String name);
}
