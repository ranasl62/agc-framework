package com.framework.agent.core;

/**
 * Thrown when {@link ToolInvocationContext} violates gateway invariants (e.g. missing trace id).
 */
public class InvalidGovernanceContextException extends RuntimeException {

    public InvalidGovernanceContextException(String message) {
        super(message);
    }
}
