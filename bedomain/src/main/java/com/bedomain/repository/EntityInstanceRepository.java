package com.bedomain.repository;

import com.bedomain.entity.EntityInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EntityInstanceRepository extends JpaRepository<EntityInstance, UUID> {

    Page<EntityInstance> findByEntityTypeId(UUID entityTypeId, Pageable pageable);

    boolean existsByEntityTypeId(UUID entityTypeId);
}
