package com.framework.agent.storage;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class TraceSequenceAllocatorTx {

    private final TraceSequenceRepository repository;

    public TraceSequenceAllocatorTx(TraceSequenceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public long allocate(String traceId) {
        Optional<TraceSequenceEntity> locked = repository.findByTraceId(traceId);
        TraceSequenceEntity row;
        if (locked.isPresent()) {
            row = locked.get();
        } else {
            try {
                row = repository.save(new TraceSequenceEntity(traceId, 0L));
            } catch (DataIntegrityViolationException ex) {
                row = repository.findByTraceId(traceId).orElseThrow();
            }
        }
        long assigned = row.getNextSeq();
        row.setNextSeq(assigned + 1);
        repository.save(row);
        return assigned;
    }
}
