package com.framework.agent.storage;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class TraceSequenceAllocator {

    private final TraceSequenceAllocatorTx tx;
    private final ConcurrentHashMap<String, Object> stripLocks = new ConcurrentHashMap<>();

    public TraceSequenceAllocator(TraceSequenceAllocatorTx tx) {
        this.tx = tx;
    }

    public long nextSequence(String traceId) {
        Object mutex = stripLocks.computeIfAbsent(traceId, k -> new Object());
        synchronized (mutex) {
            return tx.allocate(traceId);
        }
    }
}
