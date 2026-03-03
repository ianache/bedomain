package com.bedomain.repository;

import com.bedomain.domain.entity.StateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StateHistoryRepository extends JpaRepository<StateHistory, UUID> {

    List<StateHistory> findByEntityInstanceIdOrderByTimestampDesc(UUID entityInstanceId);

    Optional<StateHistory> findFirstByEntityInstanceIdOrderByTimestampDesc(UUID entityInstanceId);
}
