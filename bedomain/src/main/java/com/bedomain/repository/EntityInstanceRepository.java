package com.bedomain.repository;

import com.bedomain.domain.entity.EntityInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EntityInstanceRepository extends JpaRepository<EntityInstance, UUID> {

    Page<EntityInstance> findByEntityTypeIdAndDeletedFalse(UUID entityTypeId, Pageable pageable);

    Page<EntityInstance> findByDeletedFalse(Pageable pageable);

    Optional<EntityInstance> findByIdAndDeletedFalse(UUID id);

    boolean existsByEntityTypeId(UUID entityTypeId);
}
