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
            e.setPayloadHash(event.payloadHash());
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
        } catch (Exception ex) {
            if (meterRegistry != null) {
                meterRegistry.counter("agc.audit.write.failures").increment();
            }
            throw new AuditPersistenceException("Audit persist failed for trace " + event.traceId(), ex);
        }
    }
}
