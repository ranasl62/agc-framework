package com.framework.agent.core;

public class ToolInvocationDeniedException extends Exception {

    private final GovernanceDecision decision;

    public ToolInvocationDeniedException(GovernanceDecision decision) {
        super(decision != null ? decision.reasonCode() : "DENY");
        this.decision = decision;
    }

    public GovernanceDecision getDecision() {
        return decision;
    }
}
