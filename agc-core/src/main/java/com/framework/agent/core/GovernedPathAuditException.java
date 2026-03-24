package com.framework.agent.core;

/**
 * Thrown when a required audit write on the governed tool path fails. Execution is aborted (fail-closed).
 */
public class GovernedPathAuditException extends RuntimeException {

    public GovernedPathAuditException(String message, Throwable cause) {
        super(message, cause);
    }
}
