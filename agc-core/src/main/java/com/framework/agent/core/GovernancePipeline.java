package com.framework.agent.core;

@FunctionalInterface
public interface GovernancePipeline {

    /**
     * @return non-null decision (including {@link com.framework.agent.core.DecisionType#ALLOW}).
     */
    GovernanceDecision evaluatePreInvocation(ToolInvocationContext ctx);
}
