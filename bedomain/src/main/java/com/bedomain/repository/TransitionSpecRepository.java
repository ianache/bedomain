package com.bedomain.repository;

import com.bedomain.domain.entity.TransitionSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransitionSpecRepository extends JpaRepository<TransitionSpec, UUID> {
}
