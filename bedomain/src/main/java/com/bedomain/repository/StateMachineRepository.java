package com.bedomain.repository;

import com.bedomain.domain.entity.EntityType;
import com.bedomain.domain.entity.StateMachine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StateMachineRepository extends JpaRepository<StateMachine, UUID> {

    Page<StateMachine> findByEntityTypeAndDeletedFalse(EntityType entityType, Pageable pageable);

    Optional<StateMachine> findByEntityTypeAndDeletedFalse(EntityType entityType);

    Page<StateMachine> findByDeletedFalse(Pageable pageable);

    boolean existsByEntityTypeAndDeletedFalse(EntityType entityType);
}
