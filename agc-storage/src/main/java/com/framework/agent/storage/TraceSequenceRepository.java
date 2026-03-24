package com.framework.agent.storage;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface TraceSequenceRepository extends JpaRepository<TraceSequenceEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TraceSequenceEntity> findByTraceId(String traceId);
}
