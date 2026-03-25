package com.framework.agent.audit;

import com.framework.agent.core.AuditEvent;
import com.framework.agent.core.AuditPersistenceException;
import com.framework.agent.core.AuditRecorder;
import com.framework.agent.core.GovernanceDecision;
import com.framework.agent.core.PayloadRedactor;
import com.framework.agent.storage.AuditEventEntity;
import com.framework.agent.storage.AuditEventRepository;
import com.framework.agent.storage.TraceSequenceAllocator;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class JpaAuditRecorder implements AuditRecorder {

    private final AuditEventRepository auditEventRepository;
    private final TraceSequenceAllocator sequenceAllocator;
    private final PayloadRedactor redactor;
    private final AgcAuditProperties properties;
    private final MeterRegistry meterRegistry;

    public JpaAuditRecorder(
            AuditEventRepository auditEventRepository,
            TraceSequenceAllocator sequenceAllocator,
            PayloadRedactor redactor,
            AgcAuditProperties properties,
            ObjectProvider<MeterRegistry> meterRegistry
    ) {
        this.auditEventRepository = auditEventRepository;
        this.sequenceAllocator = sequenceAllocator;
        this.redactor = redactor;
        this.properties = properties;
        this.meterRegistry = meterRegistry.getIfAvailable();
    }

    @Override
    @Transactional
    public void record(AuditEvent event) throws AuditPersistenceException {
        Instant start = Instant.now();
        try {
            long seq = event.sequence() == AuditEvent.SEQUENCE_AUTO
                    ? sequenceAllocator.nextSequence(event.traceId())
                    : event.sequence();
            String summary = redactor.redactAndBound(event.payloadSummary(), properties.getMaxPayloadChars());
            AuditEventEntity e = new AuditEventEntity();
            e.setTraceId(event.traceId());
            e.setSequenceNum(seq);
            e.setCreatedAt(event.timestamp());
            e.setEventType(event.type().name());
            e.setToolName(event.toolName());
            e.setPayloadSummary(summary);
            String hash = event.payloadHash();
            if ((hash == null || hash.isBlank()) && properties.isHashPayload()) {
                hash = sha256(summary);
            }
            e.setPayloadHash(hash);
            if (event.decision() != null) {
                GovernanceDecision d = event.decision();
                e.setDecisionType(d.type().name());
                e.setReasonCode(d.reasonCode());
                e.setMatchedRuleIds(String.join(",", d.matchedRuleIds()));
                if (!d.details().isEmpty()) {
                    e.setDecisionDetails(d.details().entrySet().stream()
                            .map(en -> en.getKey() + "=" + en.getValue())
                            .collect(Collectors.joining(";")));
                }
            }
            auditEventRepository.save(e);
            if (meterRegistry != null) {
                long latencyMs = java.time.Duration.between(start, Instant.now()).toMillis();
                meterRegistry.timer("agc.audit.latency.ms").record(java.time.Duration.ofMillis(latencyMs));
                meterRegistry.timer("agc_audit_latency_ms").record(java.time.Duration.ofMillis(latencyMs));
            }
        } catch (Exception ex) {
            if (meterRegistry != null) {
                meterRegistry.counter("agc.audit.write.failures").increment();
                meterRegistry.counter("agc_audit_write_failures_total").increment();
            }
            throw new AuditPersistenceException("Audit persist failed for trace " + event.traceId(), ex);
        }
    }

    private static String sha256(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >>> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
