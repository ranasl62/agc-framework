package com.framework.agent.storage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "agc_audit_event")
public class AuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trace_id", nullable = false, length = 128)
    private String traceId;

    @Column(name = "sequence_num", nullable = false)
    private long sequenceNum;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "decision_type", length = 16)
    private String decisionType;

    @Column(name = "reason_code", length = 256)
    private String reasonCode;

    @Column(name = "matched_rule_ids", length = 2048)
    private String matchedRuleIds;

    @Column(name = "decision_details", length = 4000)
    private String decisionDetails;

    @Column(name = "tool_name", length = 512)
    private String toolName;

    @Column(name = "payload_summary", nullable = false, length = 4000)
    private String payloadSummary;

    @Column(name = "payload_hash", length = 128)
    private String payloadHash;

    public Long getId() {
        return id;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public long getSequenceNum() {
        return sequenceNum;
    }

    public void setSequenceNum(long sequenceNum) {
        this.sequenceNum = sequenceNum;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(String decisionType) {
        this.decisionType = decisionType;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getMatchedRuleIds() {
        return matchedRuleIds;
    }

    public void setMatchedRuleIds(String matchedRuleIds) {
        this.matchedRuleIds = matchedRuleIds;
    }

    public String getDecisionDetails() {
        return decisionDetails;
    }

    public void setDecisionDetails(String decisionDetails) {
        this.decisionDetails = decisionDetails;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getPayloadSummary() {
        return payloadSummary;
    }

    public void setPayloadSummary(String payloadSummary) {
        this.payloadSummary = payloadSummary;
    }

    public String getPayloadHash() {
        return payloadHash;
    }

    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }
}
