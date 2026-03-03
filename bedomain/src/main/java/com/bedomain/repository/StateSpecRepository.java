package com.bedomain.repository;

import com.bedomain.domain.entity.StateSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StateSpecRepository extends JpaRepository<StateSpec, UUID> {
}
