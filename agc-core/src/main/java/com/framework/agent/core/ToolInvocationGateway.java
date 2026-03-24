package com.framework.agent.core;

@FunctionalInterface
public interface ToolInvocationGateway {

    /**
     * @throws InvalidGovernanceContextException if {@code traceId}, {@code correlationId}, or {@code toolName} is missing
     * @throws ToolInvocationDeniedException if governance returns {@link com.framework.agent.core.DecisionType#DENY}
     * @throws GovernedPathAuditException if a required audit write fails (fail-closed)
     */
    ToolInvocationResult invoke(ToolInvocationContext ctx)
            throws ToolInvocationDeniedException, ToolExecutionException;
}
