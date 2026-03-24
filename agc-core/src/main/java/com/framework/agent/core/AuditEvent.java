package com.framework.agent.core;

import java.time.Instant;
import java.util.Objects;

/**
 * @param sequence Use {@code -1} when the {@link AuditRecorder} must assign the next sequence for {@code traceId}.
 */
public record AuditEvent(
        String traceId,
        long sequence,
        Instant timestamp,
        AuditEventType type,
        GovernanceDecision decision,
        String toolName,
        String payloadSummary,
        String payloadHash
) {
    public static final long SEQUENCE_AUTO = -1L;

    public AuditEvent {
        Objects.requireNonNull(traceId, "traceId");
        Objects.requireNonNull(type, "type");
        toolName = toolName != null ? toolName : "";
        payloadSummary = payloadSummary != null ? payloadSummary : "";
        payloadHash = payloadHash != null ? payloadHash : "";
        timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static AuditEvent pending(
            String traceId,
            AuditEventType type,
            GovernanceDecision decision,
            String toolName,
            String payloadSummary,
            String payloadHash
    ) {
        return new AuditEvent(traceId, SEQUENCE_AUTO, Instant.now(), type, decision, toolName, payloadSummary, payloadHash);
    }
}
