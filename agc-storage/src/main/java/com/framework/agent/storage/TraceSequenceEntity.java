package com.framework.agent.storage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "agc_trace_sequence")
public class TraceSequenceEntity {

    @Id
    @Column(name = "trace_id", length = 128, nullable = false)
    private String traceId;

    @Column(name = "next_seq", nullable = false)
    private long nextSeq;

    protected TraceSequenceEntity() {
    }

    public TraceSequenceEntity(String traceId, long nextSeq) {
        this.traceId = traceId;
        this.nextSeq = nextSeq;
    }

    public String getTraceId() {
        return traceId;
    }

    public long getNextSeq() {
        return nextSeq;
    }

    public void setNextSeq(long nextSeq) {
        this.nextSeq = nextSeq;
    }
}
