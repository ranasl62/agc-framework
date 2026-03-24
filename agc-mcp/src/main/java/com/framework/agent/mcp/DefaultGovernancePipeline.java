package com.framework.agent.mcp;

import com.framework.agent.core.GovernanceDecision;
import com.framework.agent.core.GovernancePipeline;
import com.framework.agent.core.GuardrailEvaluator;
import com.framework.agent.core.PolicyEvaluator;
import com.framework.agent.core.ToolInvocationContext;

import java.util.List;
import java.util.Map;

public class DefaultGovernancePipeline implements GovernancePipeline {

    private final PolicyEvaluator policy;
    private final GuardrailEvaluator guardrails;

    public DefaultGovernancePipeline(PolicyEvaluator policy, GuardrailEvaluator guardrails) {
        this.policy = policy;
        this.guardrails = guardrails;
    }

    @Override
    public GovernanceDecision evaluatePreInvocation(ToolInvocationContext ctx) {
        if (ctx.isPastDeadline()) {
            return GovernanceDecision.deny(
                    "CONTEXT_DEADLINE",
                    List.of("agc"),
                    Map.of("traceId", ctx.traceId())
            );
        }
        try {
            GovernanceDecision p = policy.evaluate(ctx);
            if (p == null) {
                return GovernanceDecision.deny(
                        "GOVERNANCE_CONTRACT_VIOLATION",
                        List.of("policy"),
                        Map.of("message", "PolicyEvaluator returned null")
                );
            }
            if (p.isDeny()) {
                return p;
            }
            GovernanceDecision g = guardrails.evaluate(ctx);
            if (g == null) {
                return GovernanceDecision.allow();
            }
            return g;
        } catch (RuntimeException ex) {
            return GovernanceDecision.deny(
                    "GOVERNANCE_EVALUATION_FAILED",
                    List.of("agc"),
                    Map.of("error", ex.getClass().getSimpleName(), "message", String.valueOf(ex.getMessage()))
            );
        }
    }
}
