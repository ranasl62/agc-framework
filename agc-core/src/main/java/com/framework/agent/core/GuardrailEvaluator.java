package com.framework.agent.core;

@FunctionalInterface
public interface GuardrailEvaluator {

    GovernanceDecision evaluate(ToolInvocationContext ctx);
}
