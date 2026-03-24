package com.framework.agent.core;

@FunctionalInterface
public interface GovernancePipeline {

    GovernanceDecision evaluatePreInvocation(ToolInvocationContext ctx);
}
