package com.framework.agent.mcp;

import com.framework.agent.core.GovernanceDecision;
import com.framework.agent.core.GuardrailEvaluator;
import com.framework.agent.core.PolicyEvaluator;
import com.framework.agent.core.ToolInvocationContext;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultGovernancePipelineTest {

    @Test
    void policyExceptionFailsClosedToDeny() {
        PolicyEvaluator policy = mock(PolicyEvaluator.class);
        GuardrailEvaluator guardrail = mock(GuardrailEvaluator.class);
        when(policy.evaluate(ctx("search"))).thenThrow(new RuntimeException("policy down"));
        DefaultGovernancePipeline pipeline = new DefaultGovernancePipeline(policy, guardrail);

        GovernanceDecision d = pipeline.evaluatePreInvocation(ctx("search"));
        assertEquals("GOVERNANCE_EVALUATION_FAILED", d.reasonCode());
        assertEquals("DENY", d.type().name());
    }

    @Test
    void guardrailExceptionFailsClosedToDeny() {
        PolicyEvaluator policy = mock(PolicyEvaluator.class);
        GuardrailEvaluator guardrail = mock(GuardrailEvaluator.class);
        when(policy.evaluate(ctx("search"))).thenReturn(GovernanceDecision.allow());
        when(guardrail.evaluate(ctx("search"))).thenThrow(new RuntimeException("guardrail down"));
        DefaultGovernancePipeline pipeline = new DefaultGovernancePipeline(policy, guardrail);

        GovernanceDecision d = pipeline.evaluatePreInvocation(ctx("search"));
        assertEquals("GOVERNANCE_EVALUATION_FAILED", d.reasonCode());
        assertEquals("DENY", d.type().name());
    }

    private static ToolInvocationContext ctx(String tool) {
        return new ToolInvocationContext(
                "trace",
                "corr",
                "",
                "principal",
                Set.of("user"),
                tool,
                Map.of(),
                null
        );
    }
}
