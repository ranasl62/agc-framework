package com.framework.agent.core;

@FunctionalInterface
public interface PolicyEvaluator {

    GovernanceDecision evaluate(ToolInvocationContext ctx);
}
