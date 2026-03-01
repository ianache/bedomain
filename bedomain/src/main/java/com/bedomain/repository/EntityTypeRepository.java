package com.bedomain.repository;

import com.bedomain.domain.entity.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EntityTypeRepository extends JpaRepository<EntityType, UUID> {

    Optional<EntityType> findByName(String name);

    Page<EntityType> findByDeletedFalse(Pageable pageable);

    boolean existsByNameAndDeletedFalse(String name);
}
