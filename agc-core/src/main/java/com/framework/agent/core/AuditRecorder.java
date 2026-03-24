package com.framework.agent.core;

@FunctionalInterface
public interface AuditRecorder {

    void record(AuditEvent event) throws AuditPersistenceException;
}
