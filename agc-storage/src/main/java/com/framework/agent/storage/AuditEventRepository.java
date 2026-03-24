package com.framework.agent.storage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {

    List<AuditEventEntity> findByTraceIdOrderBySequenceNumAsc(String traceId);
}
